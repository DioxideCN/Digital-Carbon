package org.sangonomiya.kotlin.param

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 *
 * @author Dioxide.CN
 * @date 2023/4/8 17:04
 * @since 1.0
 */
@ApiModel(value = "DemandFormParam对象", description = "企业咨询表提交参数")
class DemandFormParam {
    @ApiModelProperty(value = "用户昵称", required = true)
    val username: String? = null

    @ApiModelProperty(value = "企业名称", required = true)
    val companyName: String? = null

    @ApiModelProperty(value = "所属行业", required = true)
    val industry_type: String? = null

    @ApiModelProperty(value = "所属地区", required = true)
    val region_location: String? = null

    @ApiModelProperty(value = "企业需求", required = true)
    val company_needs: String? = null
}
