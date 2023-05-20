package org.sangonomiya.app.service;

import org.sangonomiya.app.core.ResponseBounce;
import org.sangonomiya.app.extension.annotation.AzygoService;
import org.sangonomiya.app.extension.annotation.CompanyOperation;
import org.sangonomiya.app.extension.annotation.RequestConsistency;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;

/**
 * @author Dioxide.CN
 * @date 2023/4/11 9:41
 * @since 1.0
 */
@Service
public interface ICollectorService {
    @RequestConsistency
    @AzygoService(keyPos = {0})
    ResponseBounce<Object> sendCreateCode(String username, String companyName, String ticket, String randstr);

    @RequestConsistency
    @CompanyOperation(operator = false)
    @AzygoService(keyPos = {0})
    ResponseBounce<Object> sendCFCreateCode(String username, String companyName, String token);

    @RequestConsistency
    @CompanyOperation(operator = false)
    ResponseBounce<Object> getAllApplication(String username, String companyName);

    ResponseBounce<Object> getSpecificApplication(String username, String companyName, String appId);

    @RequestConsistency
    @CompanyOperation(operator = false)
    ResponseBounce<Object> verifyAccountAction(String username, String companyName, String code);

    @RequestConsistency
    @CompanyOperation
    @AzygoService(keyPos = {1})
    ResponseBounce<Object> generateRSAPair(String username, String companyName, String title, Integer imgId, String appPublicKey, String remark, String notifyUrl, String code);

    @RequestConsistency
    @CompanyOperation(operator = false)
    @AzygoService(keyPos = {1})
    ResponseBounce<Object> doGenerateRSAPair(String username, String companyName, String appId, String appPublicKey);

    @RequestConsistency
    @CompanyOperation(operator = false)
    @AzygoService(keyPos = {1, 2})
    ResponseBounce<Object> modifyApplicationDefaultInformation(String username, String companyName, String appId, @Nullable String title, @Nullable String remark);

    @RequestConsistency
    @CompanyOperation(operator = false)
    @AzygoService(keyPos = {1})
    ResponseBounce<Object> doSetNotifyUrl(String username, String companyName, String appId, String notifyUrl);

    @RequestConsistency
    @CompanyOperation(operator = false)
    @AzygoService(keyPos = {1, 2})
    @Transactional(isolation = Isolation.READ_COMMITTED)
    ResponseBounce<Object> deleteApplication(String username, String companyName, String appId);

    @RequestConsistency
    @CompanyOperation(operator = false)
    @AzygoService(keyPos = {1, 2})
    @Transactional(isolation = Isolation.READ_COMMITTED)
    ResponseBounce<Object> setBindingProduct(String username, String companyName, String appId, String productId);

    @RequestConsistency
    @CompanyOperation(operator = false)
    @AzygoService(keyPos = {1, 2})
    @Transactional(isolation = Isolation.READ_COMMITTED)
    ResponseBounce<Object> enableAbility(String username, String companyName, String appId, boolean isEnable);

    @RequestConsistency
    @CompanyOperation(operator = false)
    @AzygoService(keyPos = {1, 2})
    @Transactional(isolation = Isolation.READ_COMMITTED)
    ResponseBounce<Object> publishApplication(String username, String companyName, String appId);

    @RequestConsistency
    @CompanyOperation(operator = false)
    @AzygoService(keyPos = {1, 2})
    @Transactional(isolation = Isolation.READ_COMMITTED)
    ResponseBounce<Object> archiveApplication(String username, String companyName, String appId);

    @AzygoService(keyPos = {0})
    @Transactional(isolation = Isolation.READ_COMMITTED)
    ResponseBounce<Object> applicationPortal(String appId, String encryptData);

    @RequestConsistency
    @CompanyOperation(operator = false)
    ResponseBounce<Object> getApplicationStatistic(String username, String companyName, String appId, String year, String month, int productId);
}
