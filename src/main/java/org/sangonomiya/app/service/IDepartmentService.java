package org.sangonomiya.app.service;

import org.sangonomiya.app.core.ResponseBounce;
import org.sangonomiya.app.extension.annotation.AzygoService;
import org.sangonomiya.app.extension.annotation.CompanyOperation;
import org.sangonomiya.app.extension.annotation.RequestConsistency;
import org.sangonomiya.app.extension.annotation.ShitMountain;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;

/**
 * @author Dioxide.CN
 * @date 2023/3/28 21:35
 * @since 1.0
 */
@Service
public interface IDepartmentService {
    @ShitMountain
    @RequestConsistency
    @CompanyOperation
    @AzygoService(keyPos = {0})
    @Transactional(isolation = Isolation.READ_COMMITTED)
    ResponseBounce<Object> updateDepartment(String operatorName, int departmentId, String companyName, String departmentName, @Nullable Integer parentId, String managerPhone);

    @ShitMountain
    @RequestConsistency
    @CompanyOperation
    @AzygoService(keyPos = {0})
    ResponseBounce<Object> registerDepartment(String operatorName, String companyName, String departmentName, Integer parentId, String managerName);

    @RequestConsistency
    @CompanyOperation
    ResponseBounce<Object> getDepartmentTree(String operator, String companyName, Integer disabledNode);

    @RequestConsistency
    @CompanyOperation
    @AzygoService(keyPos = {0, 1})
    @Transactional(isolation = Isolation.READ_COMMITTED)
    ResponseBounce<Object> singletonDeleteDepartment(String operatorName, String companyName, int departmentId);

    @RequestConsistency
    @CompanyOperation
    @AzygoService(keyPos = {0, 1})
    @Transactional(isolation = Isolation.READ_COMMITTED)
    ResponseBounce<Object> deepDeleteDepartment(String operatorName, String companyName, int departmentId);
}
