package org.sangonomiya.kotlin.param

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel(value = "PhoneRequest对象", description = "用户请求手机验证码时的参数(CloudFlare)")
class PhoneCFRequestParam {
    @ApiModelProperty("用户名")
    val username: String? = null

    @ApiModelProperty(value = "用户手机号", required = true)
    val phone: String? = null

    @ApiModelProperty(value = "验证码类型", required = true)
    val action: String? = null

    @ApiModelProperty(value = "验证Token", required = true)
    val token: String? = null

}