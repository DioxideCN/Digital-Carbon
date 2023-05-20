package org.sangonomiya.kotlin.controller

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.sangonomiya.app.core.ResponseBounce
import org.sangonomiya.app.service.ICollectorService
import org.springframework.web.bind.annotation.*
import javax.annotation.Resource

/**
 * @author Dioxide.CN
 * @date 2023/4/9 8:54
 * @since 1.0
 */
@Api(tags = ["CollectorController"])
@RestController
@RequestMapping("/api/collector")
class CollectorController {

    @Resource
    private lateinit var collectorService: ICollectorService

    @ApiOperation(value = "获取创建应用验证码")
    @PostMapping("/get-code")
    fun getCodeAction(
        @RequestParam("username") username: String,
        @RequestParam("companyName") companyName: String,
        @RequestParam("ticket") ticket: String,
        @RequestParam("randstr") randstr: String
    ): ResponseBounce<Any> {
        return collectorService.sendCreateCode(
            username,
            companyName,
            ticket,
            randstr
        )
    }

    @ApiOperation(value = "获取创建应用验证码(CloudFlare)")
    @PostMapping("/get-cf-code")
    fun getCFCodeAction(
        @RequestParam("username") username: String,
        @RequestParam("companyName") companyName: String,
        @RequestParam("token") token: String,
    ): ResponseBounce<Any> {
        return collectorService.sendCFCreateCode(
            username,
            companyName,
            token,
        )
    }

    @ApiOperation(value = "创建密钥前的安全验证")
    @PostMapping("/safety-action")
    fun accountSafetyAction(
        @RequestParam("username") username: String,
        @RequestParam("companyName") companyName: String,
        @RequestParam("code") code: String
    ): ResponseBounce<Any> {
        return collectorService.verifyAccountAction(
            username,
            companyName,
            code
        )
    }

    @ApiOperation(value = "获取企业应用列表")
    @GetMapping("/get-all")
    fun revertApprovalAction(
        @RequestParam("username") username: String,
        @RequestParam("companyName") companyName: String
    ): ResponseBounce<Any> {
        return collectorService.getAllApplication(
            username,
            companyName
        )
    }

    @ApiOperation(value = "获取单个企业应用")
    @GetMapping("/get-specific")
    fun getSpecificApplicationAction(
        @RequestParam("username") username: String,
        @RequestParam("companyName") companyName: String,
        @RequestParam("appId") appId: String
    ): ResponseBounce<Any> {
        return collectorService.getSpecificApplication(
            username,
            companyName,
            appId
        )
    }

    @ApiOperation(value = "创建开发者应用")
    @PostMapping("/generate")
    fun revertApprovalAction(
        @RequestParam("username") username: String,
        @RequestParam("companyName") companyName: String,
        @RequestParam("title") title: String,
        @RequestParam("imgId") imgId: Int,
        @RequestParam("appPublicKey") appPublicKey: String,
        @RequestParam("remark") remark: String,
        @RequestParam("notifyUrl") notifyUrl: String,
        @RequestParam("code") code: String
    ): ResponseBounce<Any> {
        return collectorService.generateRSAPair(
            username,
            companyName,
            title,
            imgId,
            appPublicKey,
            remark,
            notifyUrl,
            code
        )
    }

    @ApiOperation(value = "变更/生成加签")
    @PostMapping("/modify-key")
    fun doGenerateOrChangeAction(
        @RequestParam("username") username: String,
        @RequestParam("companyName") companyName: String,
        @RequestParam("appId") appId: String,
        @RequestParam("appPublicKey") appPublicKey: String
    ): ResponseBounce<Any> {
        return collectorService.doGenerateRSAPair(
            username,
            companyName,
            appId,
            appPublicKey
        )
    }

    @ApiOperation(value = "变更/设置回调地址")
    @PostMapping("/modify-notify-url")
    fun modifyApplicationNotifyUrl(
        @RequestParam("username") username: String,
        @RequestParam("companyName") companyName: String,
        @RequestParam("appId") appId: String,
        @RequestParam("notifyUrl") notifyUrl: String
    ): ResponseBounce<Any> {
        return collectorService.doSetNotifyUrl(
            username,
            companyName,
            appId,
            notifyUrl
        )
    }

    @ApiOperation(value = "变更应用基本信息")
    @PostMapping("/modify-default")
    fun modifyApplicationDefault(
        @RequestParam("username") username: String,
        @RequestParam("companyName") companyName: String,
        @RequestParam("appId") appId: String,
        @RequestParam(value = "title", required = false) title: String?,
        @RequestParam(value = "remark", required = false) remark: String?
    ): ResponseBounce<Any> {
        return collectorService.modifyApplicationDefaultInformation(
            username,
            companyName,
            appId,
            title,
            remark
        )
    }

    @ApiOperation(value = "删除应用")
    @PostMapping("/delete-application")
    fun deleteApplicationAction(
        @RequestParam("username") username: String,
        @RequestParam("companyName") companyName: String,
        @RequestParam("appId") appId: String,
    ): ResponseBounce<Any> {
        return collectorService.deleteApplication(
            username,
            companyName,
            appId
        )
    }

    @ApiOperation(value = "绑定产品")
    @PostMapping("/bind-product")
    fun bindApplicationAction(
        @RequestParam("username") username: String,
        @RequestParam("companyName") companyName: String,
        @RequestParam("appId") appId: String,
        @RequestParam("productId") productId: String
    ): ResponseBounce<Any> {
        return collectorService.setBindingProduct(
            username,
            companyName,
            appId,
            productId
        )
    }

    @ApiOperation(value = "启用额外能力")
    @PostMapping("/modify-ability")
    fun enableAbilityAction(
        @RequestParam("username") username: String,
        @RequestParam("companyName") companyName: String,
        @RequestParam("appId") appId: String,
        @RequestParam("isEnable") isEnable: Boolean
    ): ResponseBounce<Any> {
        return collectorService.enableAbility(
            username,
            companyName,
            appId,
            isEnable
        )
    }

    @ApiOperation(value = "上线应用")
    @PostMapping("/publish-app")
    fun publishAppAction(
        @RequestParam("username") username: String,
        @RequestParam("companyName") companyName: String,
        @RequestParam("appId") appId: String
    ): ResponseBounce<Any> {
        return collectorService.publishApplication(
            username,
            companyName,
            appId
        )
    }

    @ApiOperation(value = "下线应用")
    @PostMapping("/archive-app")
    fun archiveAppAction(
        @RequestParam("username") username: String,
        @RequestParam("companyName") companyName: String,
        @RequestParam("appId") appId: String
    ): ResponseBounce<Any> {
        return collectorService.archiveApplication(
            username,
            companyName,
            appId
        )
    }

    @ApiOperation(value = "查询应用统计")
    @PostMapping("/get-statistic")
    fun getApplicationStatisticAction(
        @RequestParam("username") username: String,
        @RequestParam("companyName") companyName: String,
        @RequestParam("appId") appId: String,
        @RequestParam("year") year: String,
        @RequestParam("month") month: String,
        @RequestParam("productId") productId: Int
    ): ResponseBounce<Any> {
        return collectorService.getApplicationStatistic(
            username,
            companyName,
            appId,
            year,
            month,
            productId
        )
    }

}