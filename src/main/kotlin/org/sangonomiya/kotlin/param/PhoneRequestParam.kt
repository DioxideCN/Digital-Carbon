package org.sangonomiya.kotlin.param

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * 前端需要用qs.stringify(data)进行传递
 * @author Dioxide.CN
 * @date 2023/3/6 10:02
 * @since 1.0
 */
@ApiModel(value = "PhoneRequest对象", description = "用户请求手机验证码时的参数")
class PhoneRequestParam {
    @ApiModelProperty("用户名")
    val username: String? = null

    @ApiModelProperty(value = "用户手机号", required = true)
    val phone: String? = null

    @ApiModelProperty(value = "验证码类型", required = true)
    val action: String? = null

    @ApiModelProperty(value = "验证码票据", required = true)
    val ticket: String? = null

    @ApiModelProperty(value = "验证码随机字符", required = true)
    val randstr: String? = null
}
