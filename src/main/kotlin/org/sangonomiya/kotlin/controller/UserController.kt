package org.sangonomiya.kotlin.controller

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.sangonomiya.app.core.Response
import org.sangonomiya.app.core.ResponseBounce
import org.sangonomiya.app.service.IPermissionService
import org.sangonomiya.app.service.IUserService
import org.sangonomiya.kotlin.param.*
import org.springframework.web.bind.annotation.*
import javax.annotation.Resource
import javax.servlet.http.HttpServletRequest

/**
 * @author Dioxide.CN
 * @date 2023/4/8 15:38
 * @since 1.0
 */
@Api(tags = ["UserController"])
@RestController
@RequestMapping("/api/user")
class UserController {

    @Resource
    private lateinit var userService: IUserService

    @Resource
    private lateinit var permissionService: IPermissionService

    /* 手机验证码发送业务 */

    @ApiOperation(value = "发送短信验证码")
    @PostMapping("/auth/send")
    fun eventCodeSend(
        @RequestBody param: PhoneRequestParam
    ): ResponseBounce<Any> {
        return userService.eventCodeSendAction(
            param.username,
            param.phone,
            param.action,
            param.ticket,
            param.randstr
        )
    }

    @ApiOperation(value = "登录用户发送短信验证码")
    @PostMapping("/logged-action/cf-send")
    fun loggedEventCFCodeSend(
        @RequestBody param: PhoneCFRequestSecurityParam
    ): ResponseBounce<Any> {
        return userService.eventCFCodeSendBeforeUserCheckAction(
            param.username,
            param.phone,
            param.action,
            param.token
        )
    }

    /* 手机验证码发送业务(CloudFlare) */

    @ApiOperation(value = "发送短信验证码(CloudFlare)")
    @PostMapping("/auth/cf-send")
    fun eventCFCodeSend(
        @RequestBody param: PhoneCFRequestParam
    ): ResponseBounce<Any> {
        return userService.eventCFCodeSendAction(
            param.username,
            param.phone,
            param.action,
            param.token
        )
    }

    @ApiOperation(value = "登录用户发送短信验证码(CloudFlare)")
    @PostMapping("/logged-action/send")
    fun loggedEventCodeSend(
        @RequestBody param: PhoneRequestSecurityParam
    ): ResponseBounce<Any> {
        return userService.eventCodeSendBeforeUserCheckAction(
            param.username,
            param.phone,
            param.action,
            param.ticket,
            param.randstr
        )
    }

    /* 基本账号信息CRUD操作 */

    @ApiOperation(value = "用户名登录")
    @PostMapping("/auth/login/username")
    fun loginUsername(
        @RequestParam("username") username: String,
        @RequestParam("password") password: String,
        request: HttpServletRequest
    ): ResponseBounce<Any> {
        return userService
            .loginWithUsernameAction(username, password, request)
    }

    @ApiOperation(value = "获取权限组")
    @GetMapping("/auth/query-group")
    fun getUserGroup(
        @RequestParam("username") username: String
    ): ResponseBounce<Any> {
        return permissionService.getPermissionAction(username)
    }

    @ApiOperation(value = "邮箱登录")
    @PostMapping("/auth/login/email")
    fun loginEmail(
        @RequestParam("email") email: String,
        @RequestParam("password") password: String,
        request: HttpServletRequest
    ): ResponseBounce<Any> {
        return userService
            .loginWithEmailAction(email, password, request)
    }

    @ApiOperation(value = "短信验证码登录")
    @PostMapping("/auth/login/phone")
    fun loginPhone(
        @RequestParam("phone") phone: String,
        @RequestParam("code") code: String,
        request: HttpServletRequest
    ): ResponseBounce<Any> {
        return userService
            .loginWithPhoneAction(phone, code, request)
    }

    @ApiOperation(value = "用户注册")
    @PostMapping("/auth/register")
    fun register(
        @RequestBody userRegisterParam: UserRegisterParam,
        request: HttpServletRequest
    ): ResponseBounce<Any> {
        return userService.registerAction(
            userRegisterParam.username,
            userRegisterParam.password,
            userRegisterParam.email,
            userRegisterParam.phone,
            userRegisterParam.code,
            request
        )
    }

    @ApiOperation(value = "用户修改联系方式")
    @PostMapping("/profile/phone")
    fun changePhone(
        @RequestParam("username") username: String,
        @RequestParam("password") password: String,
        @RequestParam("newPhone") newPhone: String,
        @RequestParam("code") code: String
    ): ResponseBounce<Any> {
        return userService.changePhoneAction(
            username,
            password,
            newPhone,
            code
        )
    }

    @ApiOperation(value = "用户修改头像")
    @PostMapping("/profile/update")
    fun changeProfile(
        @RequestBody param: UserUpdateParam
    ): ResponseBounce<Any> {
        return userService.changeProfileAction(
            param.username,
            param.city,
            param.gender,
            param.realname,
            param.portrait
        )
    }

    @ApiOperation(value = "用户修改密码")
    @PostMapping("/profile/change-password")
    fun changePassword(
        @RequestParam("username") username: String,
        @RequestParam("oldPassword") oldPassword: String,
        @RequestParam("newPassword") newPassword: String
    ): ResponseBounce<Any> {
        return userService.changePasswordByLoginUserAction(
            username,
            oldPassword,
            newPassword
        )
    }

    @ApiOperation(value = "用户找回密码")
    @PostMapping("/auth/find-password")
    fun findBackPassword(
        @RequestBody param: UserChangePasswordParam
    ): ResponseBounce<Any> {
        return userService.findBackPasswordAction(
            param.username,
            param.newPassword,
            param.phone,
            param.code
        )
    }

    /* 以下需要提供登录后的Token进行验证后才能执行 */

    @ApiOperation(value = "获取用户的权限组")
    @GetMapping("/profile/permission-group")
    fun getUserPermissionGroup(
        @RequestParam("username") username: String
    ): ResponseBounce<Any> {
        return Response.success(
            userService.getUserPermissionGroup(
                username
            )
        )
    }

    @ApiOperation(value = "获取用户的权限组所属的企业")
    @GetMapping("/profile/pg-of-company")
    fun getUserPermissionGroupFromWhichCompany(
        @RequestParam("username") username: String
    ): ResponseBounce<Any> {
        return userService.getUserPermissionGroupFromWhichCompany(
            username
        )
    }

}