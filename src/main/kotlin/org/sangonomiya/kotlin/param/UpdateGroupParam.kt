package org.sangonomiya.kotlin.param

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 *
 * @author Dioxide.CN
 * @date 2023/4/8 16:58
 * @since 1.0
 */
@ApiModel(value = "CreateGroup对象", description = "创建权限组参数")
class UpdateGroupParam {
    @ApiModelProperty(value = "用户昵称", required = true)
    val username: String? = null

    @ApiModelProperty(value = "企业名称", required = true)
    val companyName: String? = null

    @ApiModelProperty(value = "权限组ID", required = true)
    val groupId = 0

    @ApiModelProperty(value = "新的权限组名称", required = true)
    val groupName: String? = null

    @ApiModelProperty(value = "新的权限组", required = true)
    val permissions: Array<String>? = null

    @ApiModelProperty(value = "新的备注", required = true)
    val remark: String? = null
}
