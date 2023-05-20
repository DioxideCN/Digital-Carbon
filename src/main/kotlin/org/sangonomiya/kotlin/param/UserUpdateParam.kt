package org.sangonomiya.kotlin.param

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 *
 * @author Dioxide.CN
 * @date 2023/4/8 15:48
 * @since 1.0
 */
@ApiModel(value = "UserUpdate对象", description = "用户基础资料更新时需要传递的参数列表")
class UserUpdateParam {
    @ApiModelProperty(value = "用户昵称", required = true)
    val username: String? = null

    @ApiModelProperty("用户城市")
    val city: String? = null

    @ApiModelProperty("用户性别")
    val gender: String? = null

    @ApiModelProperty("用户姓名")
    val realname: String? = null

    @ApiModelProperty("用户头像")
    val portrait: Int? = null
}
