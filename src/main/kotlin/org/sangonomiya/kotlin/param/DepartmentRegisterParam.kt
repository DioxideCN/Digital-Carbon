package org.sangonomiya.kotlin.param

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 *
 * @author Dioxide.CN
 * @date 2023/4/8 16:46
 * @since 1.0
 */
@ApiModel(value = "DepartmentRegisterParam对象", description = "部门注册参数列表")
class DepartmentRegisterParam {
    @ApiModelProperty(value = "用户昵称", required = true)
    val operatorName: String? = null

    @ApiModelProperty(value = "企业名称", required = true)
    val companyName: String? = null

    @ApiModelProperty(value = "部门名称", required = true)
    val departmentName: String? = null

    @ApiModelProperty(value = "父部门ID", required = true)
    val parentId: Int? = null

    @ApiModelProperty(value = "管理员手机号", required = true)
    val managerPhone: String? = null
}
