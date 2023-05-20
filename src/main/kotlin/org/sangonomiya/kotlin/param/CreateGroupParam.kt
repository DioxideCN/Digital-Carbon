package org.sangonomiya.kotlin.param

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 *
 * @author Dioxide.CN
 * @date 2023/4/8 16:57
 * @since 1.0
 */
@ApiModel(value = "CreateGroup对象", description = "创建权限组参数")
class CreateGroupParam {
    @ApiModelProperty(value = "用户昵称", required = true)
    val username: String? = null

    @ApiModelProperty(value = "企业名称", required = true)
    val companyName: String? = null

    @ApiModelProperty(value = "权限组名称", required = true)
    val groupName: String? = null

    @ApiModelProperty(value = "权限组", required = true)
    val permissions: Array<String>? = null

    @ApiModelProperty(value = "备注", required = true)
    val remark: String? = null
}
