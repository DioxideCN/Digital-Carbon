package org.sangonomiya.kotlin.param

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 *
 * @author Dioxide.CN
 * @date 2023/4/9 10:47
 * @since 1.0
 */
@ApiModel(value = "ApprovalListParam对象", description = "查询OA审批表提交参数")
class ApprovalListParam {

    @ApiModelProperty(value = "查询用户", required = true)
    val username: String? = null

    @ApiModelProperty(value = "企业名称", required = true)
    val companyName: String? = null

    @ApiModelProperty(value = "页码", required = true)
    val page: Int? = null

    @ApiModelProperty(value = "每页查询数量", required = true)
    val perPageCount: Int? = null
    
}