package org.sangonomiya.kotlin.param

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 *
 * @author Dioxide.CN
 * @date 2023/4/8 15:45
 * @since 1.0
 */
@ApiModel(value = "PhoneRequestSecurity对象", description = "用户请求安全操作手机验证码时的参数")
class PhoneRequestSecurityParam {
    @ApiModelProperty(value = "用户名", required = true)
    val username: String? = null

    @ApiModelProperty("手机号")
    val phone: String? = null

    @ApiModelProperty(value = "验证码类型", required = true)
    val action: String? = null

    @ApiModelProperty(value = "验证码票据", required = true)
    val ticket: String? = null

    @ApiModelProperty(value = "验证码随机字符", required = true)
    val randstr: String? = null
}
