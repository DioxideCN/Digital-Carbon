package org.sangonomiya.app.service;

import org.sangonomiya.app.core.ResponseBounce;
import org.sangonomiya.app.entity.Company;
import org.sangonomiya.app.extension.annotation.AzygoService;
import org.sangonomiya.app.extension.annotation.CompanyOperation;
import org.sangonomiya.app.extension.annotation.RequestConsistency;
import org.sangonomiya.app.entity.UserVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;

/**
 * @author Dioxide.CN
 * @date 2023/3/4 15:41
 * @since 1.0
 */
@Service
public interface ICompanyService {

    ResponseBounce<Object> submitDemandForm(String username, String companyName, String industry_type, String region_location, String company_needs);

    ResponseBounce<Object> registerAction(String username, String companyName, String companyLocation, String companyIndustryType);

    Company getCompanyByName(String companyName);

    @Transactional(isolation = Isolation.READ_COMMITTED)
    @RequestConsistency
    @AzygoService(keyPos = {1})
    ResponseBounce<Object> updateAction(String username, String companyName, String companyIndustryType, String companyLocation);

    @RequestConsistency
    ResponseBounce<Object> getAvailableBizByUsername(String username);

    @RequestConsistency
    ResponseBounce<Company> getCompanyByUsername(String username);

    ResponseBounce<Object> relateUserToCompany(String username, String companyName);

    Company getCompanyById(int companyId);

    boolean hasInCompany(UserVO user);

    @RequestConsistency
    ResponseBounce<Object> getCompanyAllPermissionGroup(String username, String companyName, int page, int perPageCount);

    @RequestConsistency
    @CompanyOperation
    ResponseBounce<Object> lazyLoadingSelect(String username, String companyName, int page);

    @RequestConsistency
    ResponseBounce<Object> getCompanyMember(String username, String companyName, int page, int perPageCount);

    @RequestConsistency
    @CompanyOperation
    ResponseBounce<Object> sendInvitationAction(String username, String companyName, String[] sendPhoneList, String groupName, int duration);

    @Nullable
    Company inWhichCompany(UserVO user);

    @RequestConsistency
    @AzygoService(keyPos = {0})
    ResponseBounce<Object> acceptCompanyInvitation(String username, int messageId, String inviterName);

    @RequestConsistency
    @CompanyOperation
    @AzygoService(keyPos = {0, 1})
    ResponseBounce<Object> removeMemberFromCompany(String operator, String companyName, String username);

    void releaseRelation(UserVO user);
}
