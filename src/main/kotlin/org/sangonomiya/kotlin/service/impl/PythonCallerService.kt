package org.sangonomiya.kotlin.service.impl

import com.alibaba.fastjson2.JSONObject
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.sangonomiya.app.core.Response
import org.sangonomiya.app.core.ResponseBounce
import org.sangonomiya.app.extension.annotation.CompanyOperation
import org.sangonomiya.app.extension.annotation.RequestConsistency
import org.sangonomiya.app.mapper.ProductMapper
import org.sangonomiya.kotlin.service.IPythonCallerService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import javax.annotation.Resource

/**
 *
 * @author Dioxide.CN
 * @date 2023/4/10 0:24
 * @since 1.0
 */
@Service
class PythonCallerService: IPythonCallerService {

    private val okHttpClient = OkHttpClient()

    @Value("\${dataAnalysisServer.serverUrl}")
    private lateinit var serverUrl: String

    @Resource
    private lateinit var productMapper:ProductMapper

    @Value("\${dataAnalysisServer.secretKey}")
    private lateinit var secretKey: String

    /**
     * 向Django请求/api/product-emission/
     * @param username 用户名
     * @param companyId 企业名称
     * @param productId 产品ID
     */
    @RequestConsistency
    @CompanyOperation(operator = false)
    override fun requestProduct(
        username: String,
        companyId: Int,
        productId: Int
    ): ResponseBounce<Any> {
        val requestUrl: String = serverUrl + "product-emission/"

        if(productMapper.selectProductById(companyId.toString(), productId) == null)
            return Response.fail("产品不存在")

        val request = Request.Builder()
            .url(requestUrl)
            .post(MultipartBody
                .Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("companyId", companyId.toString())
                .addFormDataPart("productId", productId.toString())
                .build())
            .header("secret-key", secretKey)
            .build()
        val response = okHttpClient.newCall(request).execute()
        val result: String = response.body!!.string()
        return Response.success(JSONObject.parse(result))
    }

    /**
     * 向Django请求/api/total-emission/
     * @param username 用户名
     * @param companyId 企业名称
     */
    @RequestConsistency
    @CompanyOperation(operator = false)
    override fun requestTotal(
        username: String,
        companyId: Int
    ): ResponseBounce<Any> {
        val requestUrl: String = serverUrl + "total-emission/"

        val request = Request.Builder()
            .url(requestUrl)
            .post(MultipartBody
                .Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("companyId", companyId.toString())
                .build())
            .header("secret-key", secretKey)
            .build()
        val response = okHttpClient.newCall(request).execute()
        val result: String = response.body!!.string()
        return Response.success(JSONObject.parse(result))
    }

}