package org.sangonomiya.kotlin.controller

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.sangonomiya.app.core.ResponseBounce
import org.sangonomiya.app.service.IDepartmentService
import org.sangonomiya.kotlin.param.DepartmentRegisterParam
import org.sangonomiya.kotlin.param.DepartmentUpdateParam
import org.springframework.web.bind.annotation.*
import javax.annotation.Resource

/**
 *
 * @author Dioxide.CN
 * @date 2023/4/8 16:45
 * @since 1.0
 */

@Api(tags = ["DepartmentController"])
@RestController
@RequestMapping("/api/department")
class DepartmentController {
    
    @Resource
    private lateinit var departmentService: IDepartmentService
    
    @ApiOperation(value = "创建部门")
    @PostMapping("/register")
    fun registerAction(
        @RequestBody param: DepartmentRegisterParam
    ): ResponseBounce<Any> {
        return departmentService.registerDepartment(
            param.operatorName,
            param.companyName,
            param.departmentName,
            param.parentId,
            param.managerPhone
        )
    }

    @ApiOperation(value = "更新部门")
    @PostMapping("/update")
    fun updateAction(
        @RequestBody param: DepartmentUpdateParam
    ): ResponseBounce<Any> {
        return departmentService.updateDepartment(
            param.operatorName,
            param.departmentId,
            param.companyName,
            param.departmentName,
            param.parentId,
            param.managerPhone
        )
    }

    @ApiOperation(value = "获取部门树")
    @GetMapping("/query-all")
    fun queryTreeAction(
        @RequestParam("username") username: String,
        @RequestParam("companyName") companyName: String,
        @RequestParam(value = "disabledNode", required = false) disabledNode: Int?
    ): ResponseBounce<Any> {
        return departmentService.getDepartmentTree(
            username,
            companyName,
            disabledNode
        )
    }

    @ApiOperation(value = "单点删除部门结点")
    @PostMapping("/single-delete")
    fun singleDeleteDepartment(
        @RequestParam("username") username: String,
        @RequestParam("companyName") companyName: String,
        @RequestParam("departmentId") departmentId: Int
    ): ResponseBounce<Any> {
        return departmentService.singletonDeleteDepartment(
            username,
            companyName,
            departmentId
        )
    }

    @ApiOperation(value = "深度删除部门结点")
    @PostMapping("/deep-delete")
    fun deepDeleteDepartment(
        @RequestParam("username") username: String,
        @RequestParam("companyName") companyName: String,
        @RequestParam("departmentId") departmentId: Int
    ): ResponseBounce<Any> {
        return departmentService.deepDeleteDepartment(
            username,
            companyName,
            departmentId
        )
    }
    
}
