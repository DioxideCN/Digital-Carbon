package org.sangonomiya.app.service;

import org.sangonomiya.app.core.ResponseBounce;
import org.sangonomiya.app.extension.annotation.RequestConsistency;
import org.sangonomiya.kotlin.param.ProductCreateParam;
import org.sangonomiya.kotlin.param.ProductUpdateParam;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;

@Service
public interface IProductService {

    ResponseBounce<Object> createProductTable(String companyId);

    ResponseBounce<Object> dropProductTable(String companyId);

    @RequestConsistency
    @Transactional(isolation = Isolation.READ_COMMITTED)
    ResponseBounce<Object> createProduct(String username, String CompanyId, ProductCreateParam param);

    @RequestConsistency
    ResponseBounce<Object> deleteProduct(String username, String companyId, Integer productId);

    @RequestConsistency
    ResponseBounce<Object> updateProduct(String username, String companyId,  ProductUpdateParam param);

    @RequestConsistency
    ResponseBounce<Object> getAllById(String username, String companyId, Integer productId);

    @RequestConsistency
    ResponseBounce<Object> getProductById(String username, String companyId, Integer productId);

    @RequestConsistency
    ResponseBounce<Object> getProductList(String username, String companyId, int pageNum, int pageSize, String key);

    @RequestConsistency
    ResponseBounce<Object> getLifeCycleById(String username, String companyId, Integer productId);

    @RequestConsistency
    ResponseBounce<Object> getGeoPath(String username, String companyId, Integer productId);

    @RequestConsistency
    ResponseBounce<Object> getAllGeoPath(String username, String companyId);

    @RequestConsistency
    ResponseBounce<Object> downloadReport(String username, String companyId, Integer productId, HttpServletResponse response);
}
