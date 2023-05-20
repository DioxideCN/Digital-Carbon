package org.sangonomiya.app.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.session.RowBounds;
import org.sangonomiya.app.entity.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 公司Mapper在数据库中映射repo_company表并以Company对象的形式进行传输
 * @author Dioxide.CN
 * @date 2023/2/28 19:01
 * @since 1.0
 */
@Mapper
public interface RelateMapper {

    /**
     * 查询用户所在的权限组
     * @param user_id 用户ID
     * @return 用户所在的权限组Permission对象
     */
    Permission getUserPermissionGroup(Integer user_id);

    /**
     * 查询权限组所属的公司
     * @param permission_id 权限组ID
     * @return 所属的公司Company对象
     */
    Company getPermissionFromWhichCompany(Integer permission_id);

    /**
     * 根据user_id查询其所在的企业信息
     * @param user_id 用户主键id
     * @return 所关联的企业信息
     */
    Company getUserFromWhichCompany(Integer user_id);

    /**
     * 根据company_id和group_id选择符合条件的用户id集合
     * @param company_id 企业ID
     * @param group_id 权限组ID
     * @return 返回用户ID集合
     */
    Set<Integer> getCompanyMemberByGroup(int company_id, int group_id);

    /**
     * 根据user_id查询其所提交的企业一对一咨询表
     * @param user_id 用户主键id
     * @return 所提交的企业一对一咨询表对象
     */
    ServiceForm getUserSubmittedServiceForm(int user_id);

    /**
     * 根据user_id查询名下是否已有企业账户(即购买的套餐)
     * <p>
     * 这些判定条件包括是否过期、是否存在
     *
     * @param user_id 用户主键id
     * @return payment记录
     */
    Payment findUserWhoHoldCompanyAccount(int user_id);

    /**
     * 查询用户的所有提交的订单记录
     *
     * @param user_id 用户主键id
     * @return 订单记录集合
     */
    List<Order> queryUserAllOrder(int user_id);

    /**
     * 选择企业中所有的超级管理员
     * @param company_id 企业ID
     * @return 返回对应企业中所有超级管理员的集合
     */
    List<UserVO> selectAllOperatorFromCompany(int company_id);

    /**
     * 分页选择用户提交的审批表信息
     * @param user_id 用户ID
     * @param rowBounds 分页器
     * @return 分页选择用户提交的审批表集合
     */
    List<Approval> selectUserAllApproval(int user_id, RowBounds rowBounds);
    List<Map<Object, Object>> selectCompanyAllApproval(int company_id, RowBounds rowBounds);
    Integer countUserAllApproval(int user_id);
    Integer countCompanyAllApproval(int company_id);

    /**
     * 将对应的userId与companyId进行关联插入映射表中 <br/>
     * 形成企业与注册人的绑定关系
     * @param user_id 注册人的主键user_id
     * @param company_id 企业的主键company_id
     */
    void doRelateCompanyToUserAction(int user_id, int company_id);

    /**
     * 将对应的permissionGroupIOd与companyId进行关联插入映射表中 <br/>
     * 形成企业与权限组的绑定关系
     * @param company_id 企业的主键company_id
     * @param permission_group_id 权限组的主键permission_group_id
     */
    void doRelatePermissionToCompanyAction(int company_id, int permission_group_id);

    /**
     * 将对应的permissionGroupIOd与userId进行关联插入映射表中 <br/>
     * 形成企业与注册人的绑定关系
     * @param user_id 法定代表人的主键user_id
     * @param permission_group_id 权限组的主键permission_group_id
     */
    void doRelatePermissionToUserAction(int user_id, int permission_group_id);
    void updateUserWithPermissionGroup(int user_id, int permission_group_id);

    /**
     * 将对应的userId与form_id进行关联插入映射表中 <br/>
     * 形成申请表与法定代表人的绑定关系
     * @param user_id 申请一对一咨询的用户的user_id
     * @param form_id 申请表的主键form_id
     */
    void doRelateServiceFormToUserAction(int form_id, int user_id);

    /**
     * 将对应的approval_id与company_id和user_id进行关联
     * 表示企业中用户发起了一项审批请求
     * @param approval_id 审批表ID
     * @param company_id 企业ID
     * @param user_id 用户ID
     */
    void doRelateApprovalToUserAndCompanyAction(int approval_id, int company_id, int user_id);

    /**
     * 查询company_id下的所有企业员工
     * @param company_id 企业唯一id
     * @param rowBounds 分页
     * @return 所有员工集合
     */
    List<Map<Object, Object>> getCompanyAllMember(int company_id, RowBounds rowBounds);
    Integer countCompanyAllMember(int company_id);

    /**
     * 查询company_id下的所有权限组
     * @param company_id 企业唯一id
     * @param rowBounds 分页
     * @return 所有权限组集合
     */
    List<Permission> getCompanyAllGroup(int company_id, RowBounds rowBounds);
    Integer countCompanyAllGroup(int company_id);

    void deleteUserGroupRelation(int user_id);
    void deleteUserCompanyRelation(int user_id);
    void deleteGroupCompanyRelation(int permission_group_id);

    /**
     * 依据department_name和company_id关联查询部门
     * @param company_id 企业id
     * @param department_name 部门名称
     * @return 返回查询到的部门数据
     */
    Department findDepartmentByCompany(int company_id, String department_name);

    List<Department> findCompanyAllDepartment(int company_id);

    void doRelateDepartmentToCompanyAction(int company_id, int department_id);
    void deleteDepartmentCompanyRelation(int department_id);
}
