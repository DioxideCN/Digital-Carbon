package org.sangonomiya.kotlin.param

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 *
 * @author Dioxide.CN
 * @date 2023/4/8 16:59
 * @since 1.0
 */
@ApiModel(value = "CreateGroup对象", description = "创建权限组参数")
class DeleteGroupParam {
    @ApiModelProperty(value = "用户昵称", required = true)
    val username: String? = null

    @ApiModelProperty(value = "企业名称", required = true)
    val companyName: String? = null

    @ApiModelProperty(value = "重新分配的权限组id", required = true)
    val groupId = 0

    @ApiModelProperty(value = "被删除的权限组id", required = true)
    val deleteGroupId = 0
}
