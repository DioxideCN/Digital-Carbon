package org.sangonomiya.kotlin.controller

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.sangonomiya.app.core.ResponseBounce
import org.sangonomiya.app.entity.Company
import org.sangonomiya.app.service.ICompanyService
import org.sangonomiya.kotlin.param.CompanyInvitationParam
import org.sangonomiya.kotlin.param.CompanyRegisterParam
import org.springframework.web.bind.annotation.*
import javax.annotation.Resource

/**
 *
 * @author Dioxide.CN
 * @date 2023/4/8 16:37
 * @since 1.0
 */
@Api(tags = ["CompanyController"])
@RestController
@RequestMapping("/api/company")
class CompanyController {
    
    @Resource
    private lateinit var companyService: ICompanyService

    @ApiOperation(value = "创建企业账户")
    @PostMapping("/register")
    fun registerAction(
        @RequestBody param: CompanyRegisterParam
    ): ResponseBounce<Any> {
        return companyService.registerAction(
            param.username,
            param.companyName,
            param.companyIndustryType,
            param.companyLocation
        )
    }

    @ApiOperation(value = "更新企业账户")
    @PostMapping("/update")
    fun updateAction(
        @RequestBody param: CompanyRegisterParam
    ): ResponseBounce<Any> {
        return companyService.updateAction(
            param.username,
            param.companyName,
            param.companyIndustryType,
            param.companyLocation
        )
    }

    @ApiOperation(value = "根据用户获取其所在的企业")
    @PostMapping("/user-query")
    fun getCompanyByUsernameAction(
        @RequestParam("username") username: String
    ): ResponseBounce<Company> {
        return companyService.getCompanyByUsername(username)
    }

    @ApiOperation(value = "滚动刷新企业用户数据")
    @GetMapping("/scroll-update")
    fun scrollQueryUser(
        @RequestParam("username") username: String,
        @RequestParam("companyName") companyName: String,
        @RequestParam("page") page: Int
    ): ResponseBounce<Any> {
        return companyService.lazyLoadingSelect(
            username,
            companyName,
            page
        )
    }

    @ApiOperation(value = "获取用户有效的订单信息")
    @PostMapping("/available-biz")
    fun getAvailableBizByUsernameAction(
        @RequestParam("username") username: String
    ): ResponseBounce<Any> {
        return companyService.getAvailableBizByUsername(username)
    }

    @ApiOperation(value = "分页获取企业员工列表")
    @PostMapping("/query-member")
    fun getCompanyMembersAction(
        @RequestParam("username") username: String,
        @RequestParam("companyName") companyName: String,
        @RequestParam("page") page: Int,
        @RequestParam("perPageCount") perPageCount: Int
    ): ResponseBounce<Any> {
        return companyService.getCompanyMember(
            username,
            companyName,
            page,
            perPageCount
        )
    }

    @ApiOperation(value = "邀请成员加入企业")
    @PostMapping("/invite-message")
    fun inviteUserEnterCompany(
        @RequestBody param: CompanyInvitationParam
    ): ResponseBounce<Any> {
        return companyService.sendInvitationAction(
            param.username,
            param.companyName,
            param.sendPhoneList,
            param.groupName,
            param.duration
        )
    }

    @ApiOperation(value = "用户接受企业邀请")
    @PostMapping("/accept-invitation")
    fun acceptCompanyInvitation(
        @RequestParam("username") username: String,
        @RequestParam("messageId") messageId: Int,
        @RequestParam("inviterName") inviterName: String
    ): ResponseBounce<Any> {
        return companyService.acceptCompanyInvitation(
            username,
            messageId,
            inviterName
        )
    }

    @ApiOperation(value = "将用户移出企业")
    @PostMapping("/remove-member")
    fun removeUserFromCompany(
        @RequestParam("operatorName") operatorName: String,
        @RequestParam("companyName") companyName: String,
        @RequestParam("username") username: String
    ): ResponseBounce<Any> {
        return companyService.removeMemberFromCompany(
            operatorName,
            companyName,
            username
        )
    }


}