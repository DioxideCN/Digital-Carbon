package org.sangonomiya.kotlin.controller

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.sangonomiya.app.core.ResponseBounce
import org.sangonomiya.app.service.ICompanyService
import org.sangonomiya.app.service.IPermissionService
import org.sangonomiya.kotlin.param.CreateGroupParam
import org.sangonomiya.kotlin.param.DeleteGroupParam
import org.sangonomiya.kotlin.param.UpdateGroupParam
import org.springframework.web.bind.annotation.*
import javax.annotation.Resource

/**
 *
 * @author Dioxide.CN
 * @date 2023/4/8 16:56
 * @since 1.0
 */
@Api(tags = ["PermissionController"])
@RestController
@RequestMapping("/api/permission")
class PermissionController {
    
    @Resource
    private lateinit var permissionService: IPermissionService

    @Resource
    private lateinit var companyService: ICompanyService

    @ApiOperation(value = "创建权限组")
    @PostMapping("/create-group")
    fun createGroup(
        @RequestBody param: CreateGroupParam
    ): ResponseBounce<Any> {
        return permissionService.registerAction(
            param.username,
            param.companyName,
            param.groupName,
            param.permissions,
            param.remark
        )
    }

    @ApiOperation(value = "更新权限组")
    @PostMapping("/update-group")
    fun updateGroup(
        @RequestBody param: UpdateGroupParam
    ): ResponseBounce<Any> {
        return permissionService.updateAction(
            param.username,
            param.companyName,
            param.groupId,
            param.groupName,
            param.permissions,
            param.remark
        )
    }

    @ApiOperation(value = "删除权限组")
    @PostMapping("/delete-group")
    fun deleteGroup(
        @RequestBody param: DeleteGroupParam
    ): ResponseBounce<Any> {
        return permissionService.deleteGroupAction(
            param.username,
            param.companyName,
            param.groupId,
            param.deleteGroupId
        )
    }

    @ApiOperation(value = "分页获取企业权限组列表")
    @PostMapping("/query-group")
    fun getCompanyGroupsAction(
        @RequestParam("username") username: String?,
        @RequestParam("companyName") companyName: String?,
        @RequestParam("page") page: Int,
        @RequestParam("perPageCount") perPageCount: Int
    ): ResponseBounce<Any> {
        return companyService.getCompanyAllPermissionGroup(
            username,
            companyName,
            page,
            perPageCount
        )
    }

    @ApiOperation(value = "变更用户的权限组")
    @PostMapping("/change-group")
    fun getCompanyGroupsAction(
        @RequestParam("operatorName") operatorName: String?,
        @RequestParam("companyName") companyName: String?,
        @RequestParam("username") username: String?,
        @RequestParam("groupId") groupId: Int
    ): ResponseBounce<Any> {
        return permissionService.changeUserGroup(
            operatorName,
            companyName,
            username,
            groupId
        )
    }

}
