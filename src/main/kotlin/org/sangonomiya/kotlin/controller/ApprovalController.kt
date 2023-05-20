package org.sangonomiya.kotlin.controller

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.sangonomiya.app.core.ResponseBounce
import org.sangonomiya.app.service.IApprovalService
import org.sangonomiya.kotlin.param.ApprovalListParam
import org.sangonomiya.kotlin.param.ApprovalSubmitParam
import org.springframework.web.bind.annotation.*
import javax.annotation.Resource

/**
 * @author Dioxide.CN
 * @date 2023/4/9 8:54
 * @since 1.0
 */
@Api(tags = ["ApprovalController"])
@RestController
@RequestMapping("/api/approval")
class ApprovalController {

    @Resource
    private lateinit var approvalService: IApprovalService

    @ApiOperation(value = "提交OA审批表")
    @PostMapping("/submit")
    fun submitApprovalAction(
        @RequestBody param: ApprovalSubmitParam
    ): ResponseBounce<Any> {
        return approvalService.submitApprovalAction(
            param.username,
            param.companyName,
            param.title,
            param.emergencyLevel,
            param.content,
            param.notifyAll
        )
    }

    @ApiOperation(value = "撤销OA审批")
    @PostMapping("/revert")
    fun revertApprovalAction(
        @RequestParam("username") username: String,
        @RequestParam("companyName") companyName: String,
        @RequestParam("approvalId") approvalId: Int
    ): ResponseBounce<Any> {
        return approvalService.revertApproval(
            username,
            companyName,
            approvalId
        )
    }

    @ApiOperation(value = "申请加急OA审批")
    @PostMapping("/urge")
    fun urgeApprovalAction(
        @RequestParam("username") username: String,
        @RequestParam("companyName") companyName: String,
        @RequestParam("approvalId") approvalId: Int,
        @RequestParam("approvalTitle") approvalTitle: String
    ): ResponseBounce<Any> {
        return approvalService.urgeApproval(
            username,
            companyName,
            approvalId,
            approvalTitle
        )
    }

    @ApiOperation(value = "更改OA审批状态")
    @PostMapping("/examine")
    fun examineApprovalAction(
        @RequestParam("username") username: String,
        @RequestParam("companyName") companyName: String,
        @RequestParam("approvalId") approvalId: Int,
        @RequestParam("isPassed") isPassed: Boolean
    ): ResponseBounce<Any> {
        return approvalService.examineApproval(
            username,
            companyName,
            approvalId,
            isPassed
        )
    }

    @ApiOperation(value = "分页查询用户的OA审批表")
    @PostMapping("/list")
    fun listApprovalAction(
        @RequestBody param: ApprovalListParam
    ): ResponseBounce<Any> {
        return approvalService.getUserAllApproval(
            param.username,
            param.companyName,
            param.perPageCount!!,
            param.page!!
        )
    }

    @ApiOperation(value = "分页查询所有的OA审批表")
    @PostMapping("/list-all")
    fun listAllApprovalAction(
        @RequestBody param: ApprovalListParam
    ): ResponseBounce<Any> {
        return approvalService.getCompanyAllApproval(
            param.username,
            param.companyName,
            param.perPageCount!!,
            param.page!!
        )
    }

}