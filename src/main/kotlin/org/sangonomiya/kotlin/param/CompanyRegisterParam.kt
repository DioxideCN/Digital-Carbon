package org.sangonomiya.kotlin.param

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 *
 * @author Dioxide.CN
 * @date 2023/4/8 16:40
 * @since 1.0
 */
@ApiModel(value = "CompanyRegister对象", description = "企业注册时需要传递的所有参数")
class CompanyRegisterParam {
    @ApiModelProperty(value = "用户昵称", required = true)
    val username: String? = null

    @ApiModelProperty(value = "企业名称", required = true)
    val companyName: String? = null

    @ApiModelProperty(value = "所属区域", required = true)
    val companyLocation: String? = null

    @ApiModelProperty(value = "行业类型", required = true)
    val companyIndustryType: String? = null
}
