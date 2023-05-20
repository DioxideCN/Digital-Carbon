package org.sangonomiya.app.service;

import org.sangonomiya.app.core.ResponseBounce;
import org.sangonomiya.app.extension.annotation.AzygoService;
import org.sangonomiya.app.extension.annotation.CompanyOperation;
import org.sangonomiya.app.extension.annotation.RequestConsistency;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Dioxide.CN
 * @date 2023/4/8 9:12
 * @since 1.0
 */
@Service
public interface IApprovalService {
    @RequestConsistency
    @CompanyOperation(operator = false)
    @AzygoService(keyPos = {0})
    @Transactional(isolation = Isolation.READ_COMMITTED)
    ResponseBounce<Object> submitApprovalAction(String username, String companyName, String title, Integer emergency_level, String content, boolean notifyAll);

    @RequestConsistency
    @CompanyOperation(operator = false)
    ResponseBounce<Object> getUserAllApproval(String username, String companyName, int perPageCount, int page);

    @RequestConsistency
    @CompanyOperation
    ResponseBounce<Object> getCompanyAllApproval(String username, String companyName, int perPageCount, int page);

    @RequestConsistency
    @CompanyOperation(operator = false)
    @AzygoService(keyPos = {1, 2})
    @Transactional(isolation = Isolation.READ_COMMITTED)
    ResponseBounce<Object> revertApproval(String username, String companyName, int approvalId);

    @RequestConsistency
    @CompanyOperation(operator = false)
    @AzygoService(keyPos = {0})
    ResponseBounce<Object> urgeApproval(String username, String companyName, int approvalId, String approvalTitle);

    @RequestConsistency
    @CompanyOperation
    @AzygoService(keyPos = {1, 2})
    @Transactional(isolation = Isolation.READ_COMMITTED)
    ResponseBounce<Object> examineApproval(String username, String companyName, int approvalId, boolean isPassed);
}
