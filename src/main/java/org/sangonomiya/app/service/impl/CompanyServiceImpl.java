package org.sangonomiya.app.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.jsonwebtoken.lang.Assert;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.RowBounds;
import org.sangonomiya.app.core.NotificationHelper;
import org.sangonomiya.kotlin.Pair;
import org.sangonomiya.app.core.Response;
import org.sangonomiya.app.core.ResponseBounce;
import org.sangonomiya.app.core.message.MessageAction;
import org.sangonomiya.app.core.message.MessageBuilder;
import org.sangonomiya.app.core.message.MessageType;
import org.sangonomiya.app.entity.*;
import org.sangonomiya.app.extension.annotation.AzygoService;
import org.sangonomiya.app.extension.annotation.CompanyOperation;
import org.sangonomiya.app.extension.annotation.RequestConsistency;
import org.sangonomiya.app.extension.annotation.ShitMountain;
import org.sangonomiya.app.mapper.CompanyMapper;
import org.sangonomiya.app.mapper.NotificationMapper;
import org.sangonomiya.app.mapper.RelateMapper;
import org.sangonomiya.app.mapper.ServiceFormMapper;
import org.sangonomiya.app.service.*;
import org.sangonomiya.groovy.DataHandler;
import org.sangonomiya.groovy.DateHandler;
import org.sangonomiya.groovy.VerifyHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Dioxide.CN
 * @date 2023/2/28 16:53
 * @since 1.0
 */
@Service
@Slf4j
public class CompanyServiceImpl extends ServiceImpl<CompanyMapper, Company> implements ICompanyService {

    @Resource
    private CompanyMapper companyMapper;
    @Resource
    private IUserService userService;
    @Resource
    private IProductService productService;
    @Resource
    private IPaymentService paymentService;
    @Resource
    private IPermissionService permissionService;
    @Resource
    private INotificationService notificationService;
    @Resource
    private RelateMapper relateMapper;
    @Resource
    private ServiceFormMapper serviceFormMapper;
    @Resource
    private NotificationMapper notificationMapper;

    @Resource
    private NotificationHelper notificationHelper;

    /**
     * 企业代表人提交一对一咨询表的请求，默认一个账户只能申请一个企业账户
     *
     * @param username        申请用户的用户名
     * @param companyName     代表提交申请的企业名
     * @param industry_type   所属行业
     * @param region_location 所属地区
     * @param company_needs   企业需求
     * @return 返回提交结果封装体
     */
    @Override
    @RequestConsistency
    @AzygoService(keyPos = {0})
    public ResponseBounce<Object> submitDemandForm(String username, String companyName, String industry_type, String region_location, String company_needs) {
        UserVO user = userService.getUserByUsername(username);

        if (hasInCompanyOrSubmittedForm(user))
            return Response.fail("您已提交过申请表或已拥有企业账户");

        ServiceForm form = new ServiceForm();
        form.setCompanyName(companyName);
        form.setIndustryType(industry_type);
        form.setRegionLocation(region_location);

        serviceFormMapper.insert(form);
        return Response.success();
    }

    /**
     * 用户在购买资格后才允许注册公司账号
     * <p>
     * 这个判断受 {@link PaymentServiceImpl#hasHoldCompanyAccount(String)} 方法约束
     *
     * @param username        注册人姓名
     * @param companyName     公司名称
     * @param companyLocation 所属区域
     * @return 返回是否注册成功
     * @see PaymentServiceImpl
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    @RequestConsistency
    @AzygoService(keyPos = {0})
    public ResponseBounce<Object> registerAction(String username, String companyName, String companyIndustryType, String companyLocation) {
        // 在repo_payment中校验用户是否有一个有效的订单
        if (!paymentService.hasHoldCompanyAccount(username))
            return Response.fail("您还未开通企业账户或资质已过期");

        if (!VerifyHandler.of().company(companyName))
            return Response.fail("非法的企业名称");

        UserVO user = userService.getUserByUsername(username);
        if (hasInCompany(user))
            return Response.fail("您已拥有企业账户");

        Company company = new Company();
        company.setCompanyUserNum(1);
        company.setCompanyCreateTime(DateHandler.getDateString());
        company.setCompanyName(companyName);
        company.setCompanyLocation(companyLocation);
        company.setCompanyIndustryType(companyIndustryType);

        // 从这里开始使用Spring事务进行数据库操作保证数据一致性
        doRegisterCompany(company, user);
        doRelateCompanyToUser(user.getId(), companyName);
        return Response.success();
    }

    /**
     * 更新企业信息，该操作只能由企业注册人管理
     * @param username 操作人用户名
     * @param companyName 企业名称
     * @param companyIndustryType 企业工业类型
     * @param companyLocation 企业所属区域
     * @return 返回是否修改成功的结果，并返回修改错误的详细信息
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    @RequestConsistency
    @CompanyOperation
    @AzygoService(keyPos = {1})
    public ResponseBounce<Object> updateAction(String username, String companyName, String companyIndustryType, String companyLocation) {
        // 在repo_payment中校验用户是否有一个有效的订单
        if (!paymentService.hasHoldCompanyAccount(username))
            return Response.fail("您还未开通企业账户或资质已过期");
        UserVO member = userService.getUserByUsername(username);

        Company company = inWhichCompany(member);
        if (company == null)
            return Response.fail("企业账户不存在");
        if (!company.getCompanyName().equals(companyName))
            return Response.fail("企业名称禁止修改");
        if (!DataHandler.isAvailable(companyIndustryType, companyLocation))
            return Response.fail("无效的字段");

        company.setCompanyIndustryType(companyIndustryType);
        company.setCompanyLocation(companyLocation);
        companyMapper.updateById(company);
        return Response.success("修改成功");
    }

    /**
     * 获取用户名下的有效账单信息
     * @param username 用户信息
     * @return 返回一个可能为空的账单信息
     */
    @Override
    @RequestConsistency
    public ResponseBounce<Object> getAvailableBizByUsername(String username) {
        if (!paymentService.hasHoldCompanyAccount(username))
            return Response.fail("您还未开通企业账户或资质已过期");

        Payment payment = paymentService.getPaymentByUsername(username);
        return Response.success(payment);
    }

    /**
     * 根据用户名获取企业信息
     * @param username 用户名
     * @return 返回所在的企业
     */
    @Override
    @RequestConsistency
    public ResponseBounce<Company> getCompanyByUsername(String username) {
        UserVO user = userService.getUserByUsername(username);
        Assert.notNull(user);

        return Response.success(inWhichCompany(user));
    }

    /**
     * 采用RC事务隔离进行一致性操作
     *
     * @param username    用户名
     * @param companyName 企业名称
     * @return 返回公司注册的结果
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ResponseBounce<Object> relateUserToCompany(String username, String companyName) {
        UserVO user = userService.getUserByUsername(username);
        Company company = getCompanyByName(companyName);
        int user_id = user.getId();
        int company_id = company.getId();

        relateMapper.doRelateCompanyToUserAction(user_id, company_id);
        return Response.success();
    }

    /**
     * 注册事务将公司信息写入数据库
     *
     * @param company 待写入的公司对象
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void doRegisterCompany(Company company, UserVO user) {
        companyMapper.doRegisterCompanyAction(company);
        // 创建默认admin权限组并完成注册人与该权限组的绑定
        bindUserToDefaultAdminGroup(company, user);
        // 创建该公司对应的产品和生命周期表
        productService.createProductTable(String.valueOf(company.getId()));
    }

    /**
     * 为注册的企业和其注册人分配默认超级管理员权限
     * @param company 注册企业
     * @param user 注册人
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void bindUserToDefaultAdminGroup(Company company, UserVO user) {
        // availableCompany是为了获取企业ID进行绑定
        Company availableCompany = getCompanyByName(company.getCompanyName());
        permissionService.bindDefaultAdminGroup(user, availableCompany);
    }

    /**
     * 将对应的userId与companyId进行关联插入映射表中 <br/>
     * 形成企业与法定代表人的绑定关系
     *
     * @param userId      法定代表人的主键user_id
     * @param companyName 企业的名称company_name
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void doRelateCompanyToUser(int userId, String companyName) {
        Company company = companyMapper.selectOne(new QueryWrapper<Company>().select("id").eq("company_name", companyName));
        relateMapper.doRelateCompanyToUserAction(userId, company.getId());
    }

    /**
     * 通过企业名称获取企业基本信息
     *
     * @param companyName 企业名称
     * @return 查到的企业对象
     */
    @Override
    public Company getCompanyByName(String companyName) {
        return companyMapper.selectOne(new QueryWrapper<Company>().eq("company_name", companyName));
    }

    /**
     * 通过企业ID获取企业基本信息
     *
     * @param companyId 企业ID
     * @return 查到的企业对象
     */
    @Override
    public Company getCompanyById(int companyId) {
        return companyMapper.selectById(companyId);
    }

    /**
     * 用户是否已属于某个企业账户中
     *
     * @param user 被探测的用户
     * @return true 用户已经在某个企业中了 false 用户不在任何企业中
     */
    @Override
    public boolean hasInCompany(UserVO user) {
        return relateMapper.getUserFromWhichCompany(user.getId()) != null;
    }

    /**
     * 分页分量获取企业权限组
     * @param username 操作人
     * @param companyName 企业名称
     * @param page 页码
     * @param perPageCount 每页数量
     * @return 返回分页分量获取的结果集合
     */
    @Override
    @RequestConsistency
    public ResponseBounce<Object> getCompanyAllPermissionGroup(String username, String companyName, int page, int perPageCount) {
        if (perPageCount > 100) perPageCount= 100;
        Company awaitCompany = getCompanyByName(companyName);

        // 从上一页页末开始查询perPageCount个
        int startIndex = (page - 1) * perPageCount;

        List<Permission> groupList;
        if (page < 0 || perPageCount < 0) {
            groupList = relateMapper.getCompanyAllGroup(awaitCompany.getId(), new RowBounds());
        } else {
            groupList = relateMapper.getCompanyAllGroup(awaitCompany.getId(), new RowBounds(startIndex, perPageCount));
        }

        if (groupList.size() < 1)
            return Response.fail("没有更多数据了");

        groupList.forEach(p -> p.setCreateUser(userService.getUserById(p.getCreateUserId())));

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("total", relateMapper.countCompanyAllGroup(awaitCompany.getId()));
        resultMap.put("selected", groupList);
        return Response.success(resultMap);
    }

    /**
     * 简单分离获取企业用户方法
     * @param companyName 企业名称
     * @return 返回所有用户
     */
    private Map<String, Object> simpleGetCompanyMember(String companyName, int page, int perPageCount) {
        Company awaitCompany = getCompanyByName(companyName);

        // 从上一页页末开始查询perPageCount个
        int startIndex = (page - 1) * perPageCount;

        // 用RowBounds进行数据分页返回
        List<Map<Object, Object>> userList = relateMapper.getCompanyAllMember(awaitCompany.getId(), new RowBounds(startIndex, perPageCount));

        if (userList.size() < 1)
            return null;

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("total", relateMapper.countCompanyAllMember(awaitCompany.getId()));
        resultMap.put("selected", userList);

        return resultMap;
    }

    /**
     * 滚动懒加载用户列表，默认每页20个
     * @param username 用户名
     * @param companyName 企业名称
     * @param page 页码
     * @return 返回对应页的用户
     */
    @Override
    @RequestConsistency
    @CompanyOperation(operator = false)
    public ResponseBounce<Object> lazyLoadingSelect(String username, String companyName, int page) {
        int perPageCount = 20;
        Map<String, Object> result = simpleGetCompanyMember(companyName, page, perPageCount);
        if (result == null) return Response.fail("");

        return Response.success(result);
    }

    /**
     * 分页获取企业员工信息，需要 admin.super.* 权限
     * @param username 请求者
     * @param companyName 企业名称
     * @param page 第几页
     * @param perPageCount 每页数量
     * @return 将企业人数总数和查询页成员数量封装返回
     */
    @Override
    @RequestConsistency
    @CompanyOperation(operator = false)
    public ResponseBounce<Object> getCompanyMember(String username, String companyName, int page, int perPageCount) {
        if (perPageCount > 100) perPageCount= 100;
        Map<String, Object> result = simpleGetCompanyMember(companyName, page, perPageCount);
        if (result == null) return Response.fail("没有更多数据了");

        return Response.success(result);
    }

    /**
     * 企业管理员向目标用户群发送加入企业邀请，需要最高权限
     * @param username 发起人
     * @param companyName 企业名称
     * @param sendPhoneList 目标用户群手机号
     * @param groupName 权限组名称
     * @param duration 有效时间
     * @return 成功发送给几个人的结果封装
     */
    @Override
    @RequestConsistency
    @CompanyOperation
    @AzygoService(keyPos = {0, 1})
    public ResponseBounce<Object> sendInvitationAction(String username, String companyName, String[] sendPhoneList, String groupName, int duration) {
        UserVO sender = userService.getUserByUsername(username);
        Assert.notNull(sender);
        Company ownedCompany = inWhichCompany(sender);
        // 校验权限组是否来自企业
        if (!permissionService.isCompanyHasPermissionGroup(ownedCompany, groupName))
            return Response.fail("不存在该权限组");

        int successCount = 0;
        for (String phone : sendPhoneList) {
            if (!VerifyHandler.of().phone(phone))
                return Response.fail("手机格式错误：" + phone);
            if (phone.equals(sender.getPhone())) continue;
            // 获取用户对象
            UserVO receiver = userService.getUserByPhone(phone);
            if (receiver == null) continue;

            // 已经属于一个企业
            Company awaitUserInCompany = inWhichCompany(receiver);
            if (awaitUserInCompany != null) continue;

            // 构造消息的模式，这些消息体最终会被前端解析
            String message = new MessageBuilder()
                    .title("企业邀请")
                    .type(MessageType.INVITATION_MESSAGE)
                    .body("「" + username + "」邀请您加入企业/组织「 " + companyName + "」。接受后无论成功与否该消息将会自动删除！")
                    .action(
                        MessageAction.ACCEPT_ACTION,
                        MessageAction.DENY_ACTION,
                        MessageAction.IGNORE_ACTION)
                    .duration(duration, TimeUnit.DAYS)
                    .secret(groupName)
                    .key(companyName)
                    .build();
            boolean trigger = notificationHelper
                    .source(Pair.of(message, receiver))
                    .strictSame();
            if (!trigger) {
                // 到这里才能成功发送
                notificationService.sendMessageToClient(message, sender, receiver);
                successCount++;
            }
        }

        return Response.success("成功发送给" + successCount + "位用户");
    }

    /**
     * 用户是否已属于某个企业账户中
     *
     * @param user 被探测的用户
     * @return true 用户已经在某个企业中了 false 用户不在任何企业中
     */
    @Override
    @Nullable
    public Company inWhichCompany(UserVO user) {
        return relateMapper.getUserFromWhichCompany(user.getId());
    }

    /* 屎山勿动 谁动我和谁急 By:Dioxide.CN */
    @ShitMountain
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    @RequestConsistency
    @AzygoService(keyPos = {0})
    public ResponseBounce<Object> acceptCompanyInvitation(String username, int messageId, String inviterName) {
        // 验证是否已在企业中
        UserVO awaitUser = userService.getUserByUsername(username);
        Assert.notNull(awaitUser);
        if (hasInCompany(awaitUser))
            return Response.fail("你已属于企业用户");

        // 校验通知是否为本人拥有
        Notification invitationMessage = notificationMapper.selectById(messageId);
        if (invitationMessage.getReceiver() != awaitUser.getId())
            return Response.fail("该邀请通知不属于你");

        // 校验邀请有效时间
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        JSONObject messageJson =
                JSONObject.parseObject(invitationMessage.getMessage());
        String endingDateString = DateHandler.increaseDay(invitationMessage.getCreateTime(), messageJson.getIntValue("duration"));
        try {
            Date endingDate = dateFormat.parse(endingDateString);
            Date today = new Date();
            if (endingDate.before(today))
                return Response.fail("邀请已过期");
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        // 校验邀请人是否还是企业账号的超级管理员
        UserVO inviter = userService.getUserByUsername(inviterName);
        if (inviter == null)
            return Response.fail("邀请人不存在");
        if (invitationMessage.getSender() != inviter.getId())
            return Response.fail("邀请人不匹配");
        Permission group = userService.getUserPermissionGroup(inviter);
        if (group == null || !group.getGroupName().equals("超级管理员"))
            return Response.fail("邀请已失效");

        // 校验邀请链接中的权限组和企业
        Company awaitCompany = inWhichCompany(inviter);
        if (awaitCompany == null) return Response.fail("企业不存在");
        if (!permissionService.isCompanyHasPermissionGroup(awaitCompany, messageJson.getString("secret"))) {
            return Response.fail("邀请已失效");
        }

        // 校验通过
        doRelateCompanyToUser(awaitUser.getId(), awaitCompany.getCompanyName());
        Permission awaitGroup = permissionService.getCompanyPermissionGroupByGroupName(awaitCompany, messageJson.getString("secret"));
        if (permissionService.simpleRelateUserToGroup(awaitUser.getId(), awaitGroup.getId())) {
            // 企业人数增加
            awaitCompany.setCompanyUserNum(awaitCompany.getCompanyUserNum() + 1);
            companyMapper.updateById(awaitCompany);

            // 将消息删除处理免除后患
            notificationMapper.deleteById(invitationMessage);

            return Response.success();
        }
        return Response.fail("未知错误");
    }

    /**
     * 将用户从企业中移出
     * @param operator 操作人
     * @param companyName 企业名称
     * @param username 可怜的家伙
     * @return 返回是否移出成功，如果移出失败同时返回失败原因
     */
    @Override
    @RequestConsistency
    @CompanyOperation
    @AzygoService(keyPos = {0, 1})
    public ResponseBounce<Object> removeMemberFromCompany(String operator, String companyName, String username) {
        if (operator.equals(username)) return Response.fail("你无法移除自己");

        UserVO operatorUser = userService.getUserByUsername(operator);
        UserVO firedUser = userService.getUserByUsername(username);
        if (firedUser == null)
            return Response.fail("用户不存在");

        Company company = inWhichCompany(operatorUser);
        Assert.notNull(company);
        Company company1 = inWhichCompany(firedUser);
        if (company1 == null) return Response.fail("用户不在企业中");
        if (!company.getId().equals(company1.getId()))
            return Response.fail("用户不在企业中");

        // 移除超级管理员需要企业开户用户
        Permission firedFromGroup = userService.getUserPermissionGroup(firedUser);
        Assert.notNull(firedFromGroup);
        if (firedFromGroup.getGroupName().equals("超级管理员")) {
            if (!paymentService.hasHoldCompanyAccount(operator))
                return Response.fail("权限不足");
        }

        // 验证通过准备移除操作
        permissionService.releaseRelation(firedUser); // 移除权限组
        this.releaseRelation(firedUser); // 移出企业

        // 实时通知
        String message = new MessageBuilder()
                .title("您已被移出企业")
                .type(MessageType.SIMPLE_MESSAGE)
                .body("管理员「" + username + "」已将您移出企业「 " + companyName + "」")
                .action(MessageAction.NONE_ACTION)
                .build();
        notificationService.sendMessageToClient(message, operatorUser, firedUser);

        return Response.success();
    }

    @Override
    public void releaseRelation(UserVO user) {
        relateMapper.deleteUserCompanyRelation(user.getId());
    }

    /**
     * 用户是否已属于某个企业且已经提交过了申请表
     *
     * @param user 被探测的用户
     * @return true 用户已属于某个企业或已提交过了申请表 false 用户不在任何企业中且未提交过申请表
     */
    private boolean hasInCompanyOrSubmittedForm(UserVO user) {
        ServiceForm existForm = relateMapper.getUserSubmittedServiceForm(user.getId());

        return existForm != null || hasInCompany(user);
    }

}
