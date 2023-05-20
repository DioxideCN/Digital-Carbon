package org.sangonomiya.kotlin.controller

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.sangonomiya.app.core.ResponseBounce
import org.sangonomiya.app.service.IProductService
import org.sangonomiya.kotlin.param.ProductCreateParam
import org.sangonomiya.kotlin.param.ProductUpdateParam
import org.springframework.web.bind.annotation.*
import javax.annotation.Resource
import javax.servlet.http.HttpServletResponse

/**
 *
 * @author Dioxide.CN
 * @date 2023/4/8 17:00
 * @since 1.0
 */
@Api(tags = ["ProductController"])
@RestController
@RequestMapping("/api/product")
class ProductController {
    
    @Resource
    private lateinit var productService: IProductService
    
    @ApiOperation(value = "创建产品请求")
    @PostMapping("/create")
    fun createProduct(
        @RequestParam("username") username: String,
        @RequestParam("companyId") companyId: String,
        @RequestBody param: ProductCreateParam
    ): ResponseBounce<Any> {
        return productService.createProduct(username, companyId, param)
    }

    @ApiOperation(value = "删除产品请求")
    @PostMapping("/delete")
    fun deleteProduct(
        @RequestParam("username") username: String,
        @RequestParam("companyId") companyId: String,
        @RequestParam("productId") productId: Int
    ): ResponseBounce<Any> {
        return productService.deleteProduct(username, companyId, productId)
    }

    @ApiOperation(value = "更新产品请求")
    @PostMapping("/update")
    fun updateProduct(
        @RequestParam("username") username: String,
        @RequestParam("companyId") companyId: String,
        @RequestBody param: ProductUpdateParam
    ): ResponseBounce<Any> {
        return productService.updateProduct(username, companyId, param)
    }

    @ApiOperation(value = "获取产品全部信息请求")
    @PostMapping("/selectById")
    fun getAll(
        @RequestParam("username") username: String,
        @RequestParam("companyId") companyId: String,
        @RequestParam("productId") productId: Int
    ): ResponseBounce<Any> {
        return productService.getAllById(username, companyId, productId)
    }

    @ApiOperation(value = "获取产品请求")
    @PostMapping("/productById")
    fun getProduct(
        @RequestParam("username") username: String,
        @RequestParam("companyId") companyId: String,
        @RequestParam("productId") productId: Int
    ): ResponseBounce<Any> {
        return productService.getProductById(username, companyId, productId)
    }

    @ApiOperation(value = "获取生命周期请求")
    @PostMapping("/lifecycleById")
    fun getLifeCycle(
        @RequestParam("username") username: String,
        @RequestParam("companyId") companyId: String,
        @RequestParam("productId") productId: Int
    ): ResponseBounce<Any> {
        return productService.getLifeCycleById(username, companyId, productId)
    }

    @ApiOperation(value = "获取产品列表请求")
    @PostMapping("/selectList")
    fun getProductList(
        @RequestParam("username") username: String,
        @RequestParam("companyId") companyId: String,
        @RequestParam("page") page: Int,
        @RequestParam("pageSize") pageSize: Int,
        @RequestParam(value = "key", required = false) key: String
    ): ResponseBounce<Any> {
        return productService.getProductList(username, companyId, page, pageSize, key)
    }

    @ApiOperation(value = "获取运输路径请求")
    @PostMapping("/geoPath")
    fun getGeoPath(
        @RequestParam("username") username: String,
        @RequestParam("companyId") companyId: String,
        @RequestParam("productId") productId: Int
    ): ResponseBounce<Any> {
        return productService.getGeoPath(username, companyId, productId)
    }

    @ApiOperation(value = "获取所有运输路径请求")
    @PostMapping("/allGeoPath")
    fun getAllGeoPath(
        @RequestParam("username") username: String,
        @RequestParam("companyId") companyId: String,
    ): ResponseBounce<Any> {
        return productService.getAllGeoPath(username, companyId)
    }

    @ApiOperation(value = "下载产品报告")
    @GetMapping("/report")
    fun getReport(
        @RequestParam("username") username: String,
        @RequestParam("companyId") companyId: String,
        @RequestParam("productId") productId: Int,
        response: HttpServletResponse
    ){
        productService.downloadReport(username, companyId, productId, response);
    }
}
