package org.sangonomiya.app.service.impl;

import com.alibaba.fastjson2.annotation.JSONField;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import groovy.util.logging.Slf4j;
import io.jsonwebtoken.lang.Assert;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.sangonomiya.app.core.Response;
import org.sangonomiya.app.core.ResponseBounce;
import org.sangonomiya.app.entity.Company;
import org.sangonomiya.app.entity.Department;
import org.sangonomiya.app.entity.UserVO;
import org.sangonomiya.app.extension.annotation.AzygoService;
import org.sangonomiya.app.extension.annotation.CompanyOperation;
import org.sangonomiya.app.extension.annotation.RequestConsistency;
import org.sangonomiya.app.extension.annotation.ShitMountain;
import org.sangonomiya.app.mapper.DepartmentMapper;
import org.sangonomiya.app.mapper.RelateMapper;
import org.sangonomiya.app.service.ICompanyService;
import org.sangonomiya.app.service.IDepartmentService;
import org.sangonomiya.app.service.IUserService;
import org.sangonomiya.groovy.DateHandler;
import org.sangonomiya.groovy.VerifyHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Dioxide.CN
 * @date 2023/3/28 21:35
 * @since 1.0
 */
@Slf4j
@Service
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements IDepartmentService {

    @Resource
    private IUserService userService;
    @Resource
    private ICompanyService companyService;
    @Resource
    private RelateMapper relateMapper;

    /**
     * 获取部门树形结构，这是一个类似于多叉树森林的数据结构
     * @param operator 操作人
     * @param companyName 企业名称
     * @return 返回数组形式的部门森林结构
     */
    @Override
    @RequestConsistency
    @CompanyOperation(operator = false)
    public ResponseBounce<Object> getDepartmentTree(String operator, String companyName, @Nullable Integer disabledNode) {
        Company company = companyService.getCompanyByName(companyName);
        Assert.notNull(company);
        List<Department> departments = relateMapper.findCompanyAllDepartment(company.getId());

        List<ListableDepartment> result = new ArrayList<>();
        for (Department department : departments) {
            if (department.getParent() == null) {
                // 是父节点就进行构造
                result.add(build(department, disabledNode));
            }
        }
        return Response.success(result);
    }

    /**
     * 更新某个具体的部门
     * @param operatorName 操作员
     * @param departmentId 被更新部门ID
     * @param companyName 企业名称
     * @param departmentName 新的部门名称
     * @param parentId 新的父部门ID
     * @param managerPhone 新的负责人手机号
     * @return 返回是否更新成功，如果更新失败一并返回失败原因
     */
    @Override
    @ShitMountain
    @RequestConsistency
    @CompanyOperation(value = {0, 2})
    @AzygoService(keyPos = {0})
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ResponseBounce<Object> updateDepartment(String operatorName, int departmentId, String companyName, String departmentName, @Nullable Integer parentId, String managerPhone) {
        if (departmentId <= 0) return Response.fail("错误的来源");

        Company company = companyService.getCompanyByName(companyName);
        Assert.notNull(company);

        if (!VerifyHandler.of().company(departmentName))
            return Response.fail("非法的部门名称");

        // 多线程环境下多次删除异常
        Department awaitDepartment = safeFind(company.getId(), departmentId);
        if (awaitDepartment == null) return Response.fail("部门已被删除");

        // 部门名称发生了变化
        if (!departmentName.equals(awaitDepartment.getDepartmentName())) {
            // 校验重复
            if (safeFind(company.getId(), departmentName) != null)
                return Response.fail("已存在相同名称的部门");
        }

        // 从以前的父部门的子部门中移除
        Department oldParent = findPrev(departmentId);
        if (oldParent != null) {
            // 更新以前的父部门下的子部门
            if (oldParent.getChildren() == null) // 考虑到空子部门的情况
                oldParent.setChildren(new ArrayList<>());
            oldParent.getChildren().remove(String.valueOf(departmentId));
            this.baseMapper.updateById(oldParent);
        }

        // 为可能存在的新的父部门的子部门中添加
        Department newParent;
        if (parentId != null) {
            // 校验父部门并在这里完成双向绑定的更新
            newParent = safeFind(company.getId(), parentId);
            if (newParent == null)
                return Response.fail("父部门不存在");
            // 更新新的父部门下的子部门
            if (newParent.getChildren() == null) // 考虑到空子部门的情况
                newParent.setChildren(new ArrayList<>());
            newParent.getChildren().add(String.valueOf(departmentId));
            this.baseMapper.updateById(newParent);
        }
        awaitDepartment
                .setDepartmentName(departmentName)
                .setManager(userService.getUserByPhone(managerPhone).getId())
                .setParent(parentId);

        // 自身更新
        this.baseMapper.updateById(awaitDepartment);
        return Response.success();
    }

    /**
     * 注册部门业务
     * @param operatorName 操作人名称
     * @param companyName 企业名称
     * @param departmentName 部门名称
     * @param parentId 父部门ID
     * @param managerPhone 管理员名称
     * @return 返回部门是否注册成功，如果失败同时返回失败原因
     */
    @Override
    @ShitMountain
    @RequestConsistency
    @CompanyOperation
    @AzygoService(keyPos = {0})
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ResponseBounce<Object> registerDepartment(String operatorName, String companyName, String departmentName, @Nullable Integer parentId, String managerPhone) {
        UserVO operator = userService.getUserByUsername(operatorName);
        Assert.notNull(operator);
        Company company = companyService.inWhichCompany(operator);
        Assert.notNull(company);
        if (!VerifyHandler.of().company(departmentName))
            return Response.fail("非法的部门名称");
        // 校验重复
        Department targetDepartment = safeFind(company.getId(), departmentName);
        if (targetDepartment != null)
            return Response.fail("已存在相同名称的部门");

        Department parent = null;
        if (parentId != null) {
            // 校验父部门并在这里完成双向绑定的更新
            parent = safeFind(company.getId(), parentId);
            if (parent == null)
                return Response.fail("父部门不存在");
        }

        // 构造部门
        targetDepartment = new Department()
                .setDepartmentName(departmentName)
                .setManager(
                        userService.getUserByPhone(managerPhone).getId()
                )
                .setChildren(new ArrayList<>())
                .setParent(parentId)
                .setCreator(operator.getId())
                .setCreateTime(DateHandler.getDateString());

        this.baseMapper.insert(targetDepartment);
        // 获取插入部门的ID
        Department insertedDepartment = this.baseMapper.selectOne(new QueryWrapper<Department>()
                .eq("department_name", targetDepartment.getDepartmentName())
                .eq("creator_id", targetDepartment.getCreator())
                .eq("manager_id", targetDepartment.getManager())
                .eq("create_time", targetDepartment.getCreateTime()));
        Assert.notNull(insertedDepartment);

        if (parent != null) {
            append(parent, insertedDepartment);
            this.baseMapper.updateById(parent); // 完成父部门的子部门更新
        }
        // 建立关联
        relateMapper.doRelateDepartmentToCompanyAction(company.getId(), insertedDepartment.getId());

        return Response.success();
    }

    @Override
    @RequestConsistency
    @CompanyOperation
    @AzygoService(keyPos = {0, 1})
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ResponseBounce<Object> singletonDeleteDepartment(String operatorName, String companyName, int departmentId) {
        Company company = companyService.getCompanyByName(companyName);
        if (company == null) return Response.fail("错误的来源");

        Department department = safeFind(company.getId(), departmentId);
        if (department == null) return Response.fail("部门不存在");

        simpleDeleteDepartment(department);
        return Response.success();
    }

    /**
     * 从原子操作和并发安全角度考虑，最安全的删除部门操作应该是边删除边移动。即删除部门后将其子部门向上移动到父部门下。
     * <p>
     * 当发生深度删除时，需要另外设计自底向上的部门删除方法。。
     *
     * @param node 被操作部门结点
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void simpleDeleteDepartment(Department node) {
        // 获取父部门
        Department parent = findPrev(node.getId());
        if (node.getChildren() == null || node.getChildren().size() == 0) {
            // 部门为叶子结点
            if (parent != null) // 非孤立结点
                remove(parent, node);
        } else {
            // 将子部门全部移动至parent部门
            for (int i = 0; i < node.getChildren().size(); i++) {
                Department child = find(Integer.parseInt(node.getChildren().get(i)));
                if (child == null) continue; // 如果子部门在另一个线程中被移除
                if (parent == null) {
                    // 父部门不存在则直接从node中移除child部门 -> 这会导致node.getChildren()的数组发生变化
                    remove(node, child);
                    i--; // 指针后退
                } else {
                    // 移动部门结点
                    move(parent, child);
                }
                this.baseMapper.updateById(child); // 更新子结点
            }
        }
        if (parent != null)
            this.baseMapper.updateById(parent); // 更新父结点
        // 删除联表与节点数据
        relateMapper.deleteDepartmentCompanyRelation(node.getId());
        this.baseMapper.deleteById(node);
    }

    /**
     * 深度性移除部门结点，该操作会将传入的部门结点node下的所有子部门全部移除。
     */
    @Override
    @RequestConsistency
    @CompanyOperation
    @AzygoService(keyPos = {0, 1})
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ResponseBounce<Object> deepDeleteDepartment(String operatorName, String companyName, int departmentId) {
        Company company = companyService.getCompanyByName(companyName);
        if (company == null) return Response.fail("错误的来源");

        Department department = safeFind(company.getId(), departmentId);
        if (department == null) return Response.fail("部门不存在");

        deepDeleteDepartmentDefinition(department);

        Department parent = findPrev(departmentId);
        if (parent != null) { // 先根据背景更新父部门的子部门列表
            remove(parent, department);
            this.baseMapper.updateById(parent);
        }
        // 最后删根节点
        relateMapper.deleteDepartmentCompanyRelation(departmentId);
        this.baseMapper.deleteById(departmentId);
        return Response.success();
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void deepDeleteDepartmentDefinition(Department root) {
        if (root.getChildren() == null) {
            // 叶子结点没有子部门
            return;
        }
        for (String childStr : root.getChildren()) {
            Department child = find(Integer.parseInt(childStr));
            if (child == null) continue; // 多线程环境下脏读脏写问题
            deepDeleteDepartmentDefinition(child); // 递归遍历到叶子节点
            // 受级联控制应该先删联表再删repo
            relateMapper.deleteDepartmentCompanyRelation(child.getId());
            this.baseMapper.deleteById(child);
        }
    }

    /**
     * 同步查询企业下的某个具体部门
     * @param companyId 企业ID
     * @param departmentName 部门名称
     * @return 返回查询到的结果
     */
    @Nullable
    public synchronized Department safeFind(int companyId, String departmentName) {
        return relateMapper.findDepartmentByCompany(companyId, departmentName);
    }

    /**
     * 重写：同步查询企业下的某个具体部门
     * @param companyId 企业ID
     * @param departmentId 部门ID
     * @return 返回查询到的结果
     */
    @Nullable
    public synchronized Department safeFind(int companyId, int departmentId) {
        final Department source = find(departmentId);
        if (source == null)
            return null;
        return relateMapper.findDepartmentByCompany(companyId, source.getDepartmentName());
    }

    /**
     * 只依据ID查找部门
     * @param id 部门ID
     * @return 返回部门
     */
    @Nullable
    private synchronized Department find(int id) {
        return this.baseMapper.selectById(id);
    }

    /**
     * 根据部门ID查找其父部门
     * @param id 部门ID
     * @return 返回查询部门的父部门
     */
    @Nullable
    private synchronized Department findPrev(int id) {
        Department child = this.baseMapper.selectById(id);
        if (child.getParent() == null)
            return null;
        return this.baseMapper.selectById(child.getParent());
    }

    /**
     * 为father父部门添加child子部门
     *
     * @param father 父部门对象
     * @param child  子部门对象
     */
    private synchronized void append(@NotNull Department father, @NotNull Department child) {
        // 子部门更改父部门ID
        child.setParent(father.getId());
        // 父部门添加子部门ID
        if (father.getChildren() == null)
            father.setChildren(new ArrayList<>());
        father.getChildren().add(String.valueOf(child.getId()));
    }

    /**
     * 为father父部门移除child子部门并在数据库中更新父部门
     *
     * @param father 父部门对象
     * @param child  子部门对象
     */
    private synchronized void remove(@NotNull Department father, @NotNull Department child) {
        // 子部门删除父部门ID
        child.setParent(null);
        // 父部门删除子部门ID
        father.getChildren().remove(String.valueOf(child.getId()));
    }

    /**
     * 将一个source部门移动到一个target的部门下，如果source部门有父部门会对其父部门也进行处理。
     *
     * @param target 目标部门对象
     * @param source 被移动的子部门对象
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public synchronized void move(@NotNull Department target, @NotNull Department source) {
        Department parent = findPrev(source.getId()); // 获取source部门的父部门
        if (parent == null) { // 如果没有父部门就直接移动
            append(target, source);
            return;
        }
        // 忽略其父部门就是目标部门的移动情况
        if (parent.getId() == target.getId())
            return;
        remove(parent, source); // 从source部门的父部门移除source部门
        remove(target, parent); // 从target部门移除parent部门（如果有）
        append(target, source); // 将source部门挂载到目标部门下
    }

    /**
     * 递归构造一个可以序列化的部门树形结构
     *
     * @param root 根部门
     * @return 返回部门树
     */
    public synchronized ListableDepartment build(Department root, @Nullable Integer disabledNode) {
        if (root.getParent() != null)
            throw new IllegalArgumentException("Illegal root department object");
        return new ListableDepartment(root, disabledNode).cast();
    }

    /**
     * 支持结构化操作的部门对象
     */
    @Getter
    @Setter
    private class ListableDepartment implements Serializable {
        private final int id;
        private final Department node;
        private final List<ListableDepartment> children;
        private final boolean disabled;

        @Nullable
        @JSONField(serialize = false)
        private transient Integer disabledNode;

        public ListableDepartment(Department node, @Nullable Integer disabledNode) {
            // 相应的节点前端响应为默认不禁用
            this(node, disabledNode != null && disabledNode.equals(node.getId()));
            this.disabledNode = disabledNode;
        }

        private ListableDepartment(Department node, boolean disabled) {
            this.id = node.getId();
            this.node = node;
            this.children = new ArrayList<>();
            this.disabled = disabled;

            this.node.setCreatorName(
                    userService.getUserById(node.getCreator()).getUsername());
            this.node.setManagerName(
                    userService.getUserById(node.getManager()).getUsername()
            );
        }

        public ListableDepartment cast() {
            // 有子部门就不断递归塞入ListableDepartment类型的子部门
            if (node.getChildren() != null && node.getChildren().size() != 0) {
                for (String strId : node.getChildren()) {
                    Department input = Objects.requireNonNull(find(Integer.parseInt(strId)));
                    // 如果当前被构造的部门是disabledNode下的子部门也应该disable掉
                    if (disabled) {
                        this.children.add(new ListableDepartment(input, true).cast());
                    } else {
                        this.children.add(new ListableDepartment(input, this.disabledNode).cast());
                    }
                }
            }
            return this;
        }
    }

}
