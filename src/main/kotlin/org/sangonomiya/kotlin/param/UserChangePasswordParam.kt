package org.sangonomiya.kotlin.param

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 *
 * @author Dioxide.CN
 * @date 2023/4/8 15:49
 * @since 1.0
 */
@ApiModel(value = "UserChangePasswordParam对象", description = "用户修改密码时需要传递的所有参数")
class UserChangePasswordParam {
    @ApiModelProperty(value = "用户昵称", required = true)
    val username: String? = null

    @ApiModelProperty(value = "新密码", required = true)
    val newPassword: String? = null

    @ApiModelProperty(value = "用户手机号", required = true)
    val phone: String? = null

    @ApiModelProperty(value = "验证码", required = true)
    val code: String? = null
}
