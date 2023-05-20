package org.sangonomiya.kotlin.controller

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.sangonomiya.app.core.ResponseBounce
import org.sangonomiya.kotlin.service.IPythonCallerService
import org.springframework.web.bind.annotation.*
import javax.annotation.Resource

/**
 * PythonCaller通过前端调用Spring接口间接地向Django发送请求，其目的是为了直接使用Spring已有的业务做鉴权和非法拦截降低代码重复度降低耦合
 * @author Dioxide.CN
 * @date 2023/4/10 8:38
 * @since 1.0
 */
@Api(tags = ["PythonCallerController"])
@RestController
@RequestMapping("/api/python")
class PythonCallerController {

    @Resource
    private lateinit var pythonCallerService: IPythonCallerService

    @ApiOperation(value = "计算单个产品排放")
    @GetMapping("/product-emission")
    fun productEmissionRequest(
        @RequestParam("username") username: String,
        @RequestParam("companyId") companyId: String,
        @RequestParam("productId") productId: Int
    ): ResponseBounce<Any> {
        return pythonCallerService.requestProduct(
            username,
            companyId.toInt(),
            productId
        )
    }

    @ApiOperation(value = "计算企业总排放")
    @GetMapping("/total-emission")
    fun totalEmissionRequest(
        @RequestParam("username") username: String,
        @RequestParam("companyId") companyId: String
    ): ResponseBounce<Any> {
        return pythonCallerService.requestTotal(
            username,
            companyId.toInt()
        )
    }

}