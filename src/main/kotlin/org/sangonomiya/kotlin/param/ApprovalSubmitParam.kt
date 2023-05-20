package org.sangonomiya.kotlin.param

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 *
 * @author Dioxide.CN
 * @date 2023/4/9 8:56
 * @since 1.0
 */
@ApiModel(value = "ApprovalSubmitParam对象", description = "OA审批表提交参数")
class ApprovalSubmitParam {

    @ApiModelProperty(value = "提交用户", required = true)
    val username: String? = null

    @ApiModelProperty(value = "企业名称", required = true)
    val companyName: String? = null

    @ApiModelProperty(value = "审批表标题", required = true)
    val title: String? = null

    @ApiModelProperty(value = "审批表紧急程度", required = true)
    val emergencyLevel = 0

    @ApiModelProperty(value = "审批表正文", required = true)
    val content: String? = null

    @ApiModelProperty(value = "是否通知管理", required = true)
    val notifyAll = false
    
}