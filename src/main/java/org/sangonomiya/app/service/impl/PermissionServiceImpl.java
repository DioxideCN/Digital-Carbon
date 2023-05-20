package org.sangonomiya.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.jsonwebtoken.lang.Assert;
import org.apache.ibatis.session.RowBounds;
import org.sangonomiya.app.core.Response;
import org.sangonomiya.app.core.ResponseBounce;
import org.sangonomiya.app.entity.Company;
import org.sangonomiya.app.entity.Permission;
import org.sangonomiya.app.extension.annotation.AzygoService;
import org.sangonomiya.app.extension.annotation.CompanyOperation;
import org.sangonomiya.app.extension.annotation.RequestConsistency;
import org.sangonomiya.app.mapper.PermissionMapper;
import org.sangonomiya.app.mapper.RelateMapper;
import org.sangonomiya.app.service.ICompanyService;
import org.sangonomiya.app.service.IPaymentService;
import org.sangonomiya.app.service.IPermissionService;
import org.sangonomiya.app.service.IUserService;
import org.sangonomiya.app.entity.UserVO;
import org.sangonomiya.groovy.DateHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

/**
 * 权限组业务，页面权限如下：
 * --------------------------------------
 * 数据采样 manage.data-sampling
 * 产品管理 manage.product-system
 * 碳排报告 manage.emission-report
 * 碳排分析 manage.emission-analyze
 * 定时任务 manage.interval-task
 * 数据大屏 manage.data-screen
 * 影响因子库 manage.impact-factor
 * --------------------------------------
 * 企业信息 company.overview
 * 部门管理 company.apartment-manager
 * 权限组管理 company.group-manager
 * 成员管理 company.member-manager
 * 绿色认证 company.green-certified
 * 操作审批 company.operation-approval
 * --------------------------------------
 *
 * @author Dioxide.CN
 * @date 2023/3/5 13:10
 * @since 1.0
 */
@Service
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements IPermissionService {

    @Resource
    private IUserService userService;
    @Resource
    private ICompanyService companyService;
    @Resource
    private IPaymentService paymentService;
    @Resource
    private RelateMapper relateMapper;

    public static final String[] groups = {
            // admin 管理员
            "admin.super.*", "admin.manage.*", "admin.company.*",
            // manage 管理
            "manage.data-sampling", "manage.product-system",
            "manage.emission-report", "manage.emission-analyze",
            "manage.interval-task", "manage.data-screen",
            "manage.impact-factor",
            // company 企业
            "company.overview", "company.apartment-manager",
            "company.group-manager", "company.member-manager",
            "company.green-certified", "company.operation-approval"
    };

    /**
     * 企业管理员创建权限组，判断用户是否是管理员的条件将被封装为一个切面 <code>@RequireAccess</code>
     * <p>
     * <code>@RequireAccess</code> 注解会判断第一个参数username用户是否是企业账户的创建者，当属性 strict 为 true 时其所在的权限组中必须拥有 [company.apartment-manager, company.group-manager, company.member-manager] 三个中所有权限 false 时只需包含其中一个即可。
     * <p>
     * 理论上应该允许企业重复创建相同权限类型的权限组但是其权限组名必须不相同，权限组名只允许（中文、英文、数字、下划线）的形式。
     *
     * @param username 用户名
     * @param companyName 企业名称
     * @param groupName 权限组名称
     * @param permissions 权限组应该是以数组的形式被传递过来
     * @param remark 备注
     * @return 返回是否创建成功
     */
    @Override
    @RequestConsistency
    @CompanyOperation(operator = false)
    @AzygoService(keyPos = {0})
    public ResponseBounce<Object> registerAction(String username, String companyName, String groupName, String[] permissions, String remark) {
        if (groupName.equals("超级管理员"))
            return Response.fail("超级管理员权限组已存在");

        // 用户和企业的关系已经验证通过
        UserVO operator = userService.getUserByUsername(username);
        Assert.notNull(operator);
        if (permissions.length == groups.length - 1)
            return Response.fail("超级管理员权限组已存在");
        Company company = companyService.getCompanyByName(companyName);
        Permission group = getCompanyPermissionGroupByGroupName(company, groupName);
        if (group != null)
            return Response.fail("相同名称的权限组已存在");

        // 允许权限组名称不同但权限组相同的权限组
        String groupDetail = mergePermissions(permissions);
        Permission permission = new Permission()
                .setGroupName(groupName)
                .setPermissions(groupDetail)
                .setRemark(remark)
                .setCreateUserId(operator.getId())
                .setCreateTime(DateHandler.getDateString());

        this.baseMapper.insert(permission);
        // 为后续插入绑定关系做准备
        Permission existGroup = this.baseMapper.selectOne(new QueryWrapper<Permission>()
                .eq("group_name", permission.getGroupName())
                .eq("permissions", permission.getPermissions())
                .eq("create_user_id", permission.getCreateUserId()));
        Assert.notNull(existGroup);
        // 将group与company绑定
        relateMapper.doRelatePermissionToCompanyAction(company.getId(), existGroup.getId());
        return Response.success();
    }

    /**
     * 更新权限组基本信息
     * @param username 操作用户名
     * @param companyName 所属企业名
     * @param groupId 修改的权限组ID
     * @param newGroupName 新的权限组名字
     * @param newPermissions 新的权限组
     * @param newRemark 新的权限组备注
     * @return 返回是否更新成功，并返回更新失败的具体原因
     */
    @Override
    @RequestConsistency
    @CompanyOperation(operator = false)
    @AzygoService(keyPos = {0})
    public ResponseBounce<Object> updateAction(String username, String companyName, int groupId, String newGroupName, String[] newPermissions, String newRemark) {
        if (newGroupName.equals("超级管理员") || groupId == 1)
            return Response.fail("禁止编辑超级管理员权限组");
        // 用户和企业的关系已经验证通过
        UserVO operator = userService.getUserByUsername(username);
        Assert.notNull(operator);
        if (newPermissions.length == groups.length - 1)
            return Response.fail("禁止编辑超级管理员权限组");

        // 校验重复名称
        Company company = companyService.getCompanyByName(companyName);
        Permission group = getCompanyPermissionGroupByGroupName(company, newGroupName);
        if (group != null)
            return Response.fail("相同名称的权限组已存在");

        Permission currentGroup = this.baseMapper.selectById(groupId);
        String groupDetail = mergePermissions(newPermissions);
        // 更新操作
        if (!currentGroup.getPermissions().equals(groupDetail)) {
            currentGroup.setPermissions(groupDetail);
        }
        if (!currentGroup.getGroupName().equals(newGroupName)) {
            currentGroup.setGroupName(newGroupName);
        }
        if (!currentGroup.getRemark().equals(newRemark)) {
            currentGroup.setRemark(newRemark);
        }
        this.baseMapper.updateById(currentGroup);
        return Response.success();
    }

    private String mergePermissions(final String[] permissions) {
        StringJoiner joiner = new StringJoiner(",");
        boolean manageFlag = false;
        boolean companyFlag = false;
        for (String permission : permissions) {
            if (permission.equals(groups[1])) {
                manageFlag = true;
            } else if (permission.equals(groups[2])) {
                companyFlag = true;
            } else {
                continue;
            }
            joiner.add(permission);
        }

        for (String permission : permissions) {
            if (manageFlag && permission.contains("manage.")) {
                continue;
            }
            if (companyFlag && permission.contains("company.")) {
                continue;
            }
            joiner.add(permission);
        }

        return joiner.toString();
    }

    /**
     * 删除权限组
     * @param username 操作用户名
     * @param companyName 企业名称
     * @param groupId 重新分配的权限组id
     * @param deleteGroupId 被删除的权限组id
     * @return 返回是否删除且重新分配成功，如果失败则返回失败原因
     */
    @Override
    @RequestConsistency
    @CompanyOperation(operator = false)
    @AzygoService(keyPos = {0})
    public ResponseBounce<Object> deleteGroupAction(String username, String companyName, int groupId, int deleteGroupId) {
        if (deleteGroupId == 1)
            return Response.fail("禁止删除超级管理员权限组");
        UserVO operator = userService.getUserByUsername(username);
        Company company = companyService.getCompanyByName(companyName);
        Permission operatorGroup = userService.getUserPermissionGroup(operator);
        if (operatorGroup == null ||
                (groupId == 1 && operatorGroup.getId() != 1)) return Response.fail("权限不足");

        // 防止另一边也在删除该需要重新分配的权限组
        if (!isCompanyHasPermissionGroup(company, groupId))
            return Response.fail("重新分配的权限组不存在");

        // 校验完毕重新分配权限组
        Set<Integer> allocatingUserIds = relateMapper.getCompanyMemberByGroup(company.getId(), deleteGroupId);
        for (Integer userId : allocatingUserIds) {
            relateMapper.updateUserWithPermissionGroup(userId, groupId);
        }
        // 删除权限组
        relateMapper.deleteGroupCompanyRelation(deleteGroupId);
        this.baseMapper.deleteById(deleteGroupId);
        return Response.success();
    }

    /**
     * 将用户和企业与默认超级管理员权限组完成双向绑定
     * @param user 用户对象
     * @param company 企业对象（getId不能为空）
     * @return 是否绑定成功
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public boolean bindDefaultAdminGroup(UserVO user, Company company) {
        createDefaultGlobalAdminGroup();
        // 企业relate_company_permission
        relateMapper.doRelatePermissionToCompanyAction(company.getId(), 1);
        relateMapper.doRelatePermissionToUserAction(user.getId(), 1);

        // 创建一个普通用户组隶属于user和company
        Permission defaultGroup = new Permission();
        defaultGroup.setPermissions("");
        defaultGroup.setRemark("默认用户组");
        defaultGroup.setGroupName("默认员工");
        defaultGroup.setCreateUserId(user.getId());
        defaultGroup.setCreateTime(DateHandler.getDateString());

        this.baseMapper.insert(defaultGroup);
        // 因为是第一次创建默认的所以user只有一个默认普通组
        Permission defaultCreated = this.baseMapper.selectOne(new QueryWrapper<Permission>()
                .eq("group_name", "默认员工")
                .eq("create_user_id", user.getId()));
        // 完成默认组的企业双绑
        relateMapper.doRelatePermissionToCompanyAction(company.getId(), defaultCreated.getId());

        return true;
    }

    /**
     * 变更企业用户所在的权限组
     * @param operatorName 操作人名称
     * @param companyName 企业名称
     * @param username 被操作用户名
     * @param groupId 权限组id
     * @return 返回是否修改成功和修改失败的信息
     */
    @Override
    @RequestConsistency
    @CompanyOperation
    @AzygoService(keyPos = {0, 1})
    public ResponseBounce<Object> changeUserGroup(String operatorName, String companyName, String username, int groupId) {
        if (operatorName.equals(username)) return Response.fail("你无法为自己分配权限组");
        UserVO operator = userService.getUserByUsername(operatorName);
        UserVO awaiter = userService.getUserByUsername(username);
        if (awaiter == null)
            return Response.fail("用户不存在");

        // awaiter当前所属的权限组
        Permission currentGroup = userService.getUserPermissionGroup(awaiter);
        if (currentGroup == null) return Response.fail("未知错误");

        // 重新分配给awaiter的权限组
        Permission awardGroup = this.baseMapper.selectById(groupId);
        if (currentGroup.getId() == awardGroup.getId())
            return Response.fail("权限组无变化");

        if (currentGroup.getId() == 1 || awardGroup.getId() == 1) {
            // 由超级管理员降职或升职为超级管理员的操作必须由企业账号开通者执行
            if (!paymentService.hasHoldCompanyAccount(operatorName)) {
                return Response.fail("权限不足");
            }
        }

        Company operatorCompany = companyService.inWhichCompany(operator);
        Company awaiterCompany = companyService.inWhichCompany(awaiter);
        if (operatorCompany == null || awaiterCompany == null)
            return Response.fail("未知错误");
        if (!operatorCompany.getId().equals(awaiterCompany.getId()))
            return Response.fail("企业账户不匹配");
        // 该非管理员权限组是否不存在
        if (awardGroup.getId() != 1) {
            Company awardGroupCompany = getPermissionGroupFromWhichCompany(awardGroup);
            if (!awardGroupCompany.getId().equals(operatorCompany.getId()))
                return Response.fail("权限组不存在");
        }

        // 验证全部通过后再修改权限组
        int userId = awaiter.getId();
        int awardId = awardGroup.getId();
        relateMapper.updateUserWithPermissionGroup(userId, awardId);
        return Response.success();
    }

    @Override
    @RequestConsistency
    public ResponseBounce<Object> getPermissionAction(String username) {
        UserVO user = userService.getUserByUsername(username);
        Permission group = relateMapper.getUserPermissionGroup(user.getId());
        if (group == null)
            return Response.fail("权限组不存在请联系企业管理员");
        return Response.success(group.getPermissions().split(","));
    }

    /**
     * simpleRelateUserToGroup 是线程不安全且操作不安全的将用户与权限组关联方法
     * 当且仅当验证过用户只有一种权限关系且该权限属于目标企业后才可以使用该方法
     *
     * @param userId 用户ID
     * @param groupId 权限组ID
     * @return 是否绑定成功
     */
    @Override
    public boolean simpleRelateUserToGroup(int userId, int groupId) {
        relateMapper.doRelatePermissionToUserAction(userId, groupId);
        return true;
    }

    /**
     * 创建全局超级管理员权限组，其主键id必定是1，如果发生其位置被占用则会直接覆写超级管理员组
     * <p>
     * 超级管理员组不属于任何企业账户且不会与任何企业账户相关联，但是会和用户进行关联
     * <p>
     * 在 {@link UserServiceImpl#getUserPermissionGroupFromWhichCompany(String)} 中会被禁止返回企业
     */
    public void createDefaultGlobalAdminGroup() {
        Permission awaitGroup = this.getBaseMapper().selectOne(new QueryWrapper<Permission>()
                .eq("id", 1));

        // TODO 将在程序启动时被设计
        Permission correctGroup = new Permission();
        correctGroup.setId(1);
        correctGroup.setGroupName("超级管理员");
        correctGroup.setPermissions(groups[0]);
        correctGroup.setRemark("系统默认权限组禁止删除");
        correctGroup.setCreateTime(DateHandler.getDateString());
        correctGroup.setCreateUserId(1);

        if (awaitGroup != null && awaitGroup.getPermissions().equals(groups[0])) {
            this.baseMapper.updateById(correctGroup);
            return;
        }

        this.baseMapper.insert(correctGroup);
    }

    /**
     * 根据权限组获取其所属的企业，该方法无法用于超级管理员组的检测
     * @param permission 权限组
     * @return 返回权限组所属公司
     */
    @Override
    public Company getPermissionGroupFromWhichCompany(Permission permission) {
        if (permission == null)
            return null;
        return relateMapper.getPermissionFromWhichCompany(permission.getId());
    }

    /**
     * 判断企业是否拥有某个权限组
     * @param company 企业对象
     * @param groupName 权限组名称
     * @return true 存在 false 不存在
     */
    @Override
    public boolean isCompanyHasPermissionGroup(Company company, String groupName) {
        return getCompanyPermissionGroupByGroupName(company, groupName) != null;
    }

    private boolean isCompanyHasPermissionGroup(Company company, int groupId) {
        Permission permission = this.baseMapper.selectById(groupId);
        if (permission == null) return false;
        return getCompanyPermissionGroupByGroupName(company, permission.getGroupName()) != null;
    }

    /**
     * 查找企业中的某个权限组并将其返回
     * @param company 企业对象
     * @param groupName 权限组名称
     * @return 可能为空的权限组
     */
    @Nullable
    @Override
    public Permission getCompanyPermissionGroupByGroupName(Company company, String groupName) {
        List<Permission> awaitGroup = relateMapper.getCompanyAllGroup(company.getId(), new RowBounds());

        if (groupName.equals("超级管理员")) return this.baseMapper.selectById(1);
        if (awaitGroup.size() == 1) return null;

        for (Permission group : awaitGroup) {
            if (group.getGroupName().equals(groupName))
                return group;
        }
        return null;
    }

    @Override
    public void releaseRelation(UserVO user) {
        relateMapper.deleteUserGroupRelation(user.getId());
    }

}
