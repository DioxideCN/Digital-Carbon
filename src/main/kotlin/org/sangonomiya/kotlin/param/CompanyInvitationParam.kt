package org.sangonomiya.kotlin.param

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 *
 * @author Dioxide.CN
 * @date 2023/4/8 16:41
 * @since 1.0
 */
@ApiModel(value = "CompanyInvitation对象", description = "企业发起邀请时需要传递的所有参数")
class CompanyInvitationParam {
    @ApiModelProperty(value = "用户昵称", required = true)
    val username: String? = null

    @ApiModelProperty(value = "企业名称", required = true)
    val companyName: String? = null

    @ApiModelProperty(value = "受邀名单", required = true)
    val sendPhoneList: Array<String>? = null

    @ApiModelProperty(value = "权限组名称", required = true)
    val groupName: String? = null

    @ApiModelProperty(value = "有效时间", required = true)
    val duration = 0
}
