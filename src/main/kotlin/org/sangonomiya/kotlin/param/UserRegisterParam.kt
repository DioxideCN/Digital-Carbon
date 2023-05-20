package org.sangonomiya.kotlin.param

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 *
 * @author Dioxide.CN
 * @date 2023/4/8 15:47
 * @since 1.0
 */
@ApiModel(value = "UserRegister对象", description = "用户注册时需要传递该对象中的所有参数")
class UserRegisterParam {
    @ApiModelProperty(value = "用户名", required = true)
    val username: String? = null

    @ApiModelProperty("用户邮箱")
    val email: String? = null

    @ApiModelProperty(value = "密码", required = true)
    val password: String? = null

    @ApiModelProperty(value = "联系方式", required = true)
    val phone: String? = null

    @ApiModelProperty(value = "手机验证码", required = true)
    val code: String? = null
}
