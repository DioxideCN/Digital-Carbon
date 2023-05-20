package org.sangonomiya.app.service;

import org.sangonomiya.app.core.ResponseBounce;
import org.sangonomiya.app.entity.Company;
import org.sangonomiya.app.entity.Permission;
import org.sangonomiya.app.extension.annotation.AzygoService;
import org.sangonomiya.app.extension.annotation.CompanyOperation;
import org.sangonomiya.app.extension.annotation.RequestConsistency;
import org.sangonomiya.app.entity.UserVO;
import org.springframework.stereotype.Service;

/**
 * @author Dioxide.CN
 * @date 2023/3/5 13:10
 * @since 1.0
 */
@Service
public interface IPermissionService {

    @RequestConsistency
    @CompanyOperation(operator = false)
    @AzygoService(keyPos = {0})
    ResponseBounce<Object> registerAction(String username, String companyName, String groupName, String[] permissions, String remark);

    @RequestConsistency
    @CompanyOperation(operator = false)
    @AzygoService(keyPos = {0})
    ResponseBounce<Object> updateAction(String username, String companyName, int groupId, String newGroupName, String[] newPermissions, String newRemark);

    @RequestConsistency
    @CompanyOperation(operator = false)
    @AzygoService(keyPos = {0})
    ResponseBounce<Object> deleteGroupAction(String username, String companyName, int groupId, int deleteGroupId);

    boolean bindDefaultAdminGroup(UserVO user, Company company);

    @RequestConsistency
    @CompanyOperation
    @AzygoService(keyPos = {0, 1})
    ResponseBounce<Object> changeUserGroup(String operatorName, String companyName, String username, int groupId);

    @RequestConsistency
    ResponseBounce<Object> getPermissionAction(String username);

    boolean simpleRelateUserToGroup(int userId, int groupId);

    @RequestConsistency
    Company getPermissionGroupFromWhichCompany(Permission permission);

    boolean isCompanyHasPermissionGroup(Company company, String groupName);

    Permission getCompanyPermissionGroupByGroupName(Company company, String groupName);

    void releaseRelation(UserVO user);
}
