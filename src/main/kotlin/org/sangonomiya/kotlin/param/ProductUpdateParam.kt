package org.sangonomiya.kotlin.param

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.util.*

/**
 *
 * @author Dioxide.CN
 * @date 2023/4/8 17:03
 * @since 1.0
 */
@ApiModel(value = "ProductUpdate参数对象", description = "更新产品时需要传递该对象中的所有参数")
class ProductUpdateParam {
    @ApiModelProperty(value = "产品id", required = true)
    val id = 0

    @ApiModelProperty(value = "产品名称")
    val product_name: String? = null

    @ApiModelProperty(value = "产品类型")
    val product_type: String? = null

    @ApiModelProperty(value = "产品型号")
    val product_model: String? = null

    @ApiModelProperty(value = "数量")
    val product_amount: Int? = null

    @ApiModelProperty(value = "单位")
    val product_unit: String? = null

    @ApiModelProperty(value = "产品重量")
    val product_weight: Float? = null

    @ApiModelProperty(value = "生命周期类型")
    val product_lifecycle: Boolean? = null

    @ApiModelProperty(value = "产品图片地址")
    val product_pic: String? = null

    @ApiModelProperty(value = "产品统计开始时间")
    val statistics_period_before: Date? = null

    @ApiModelProperty(value = "产品统计终止时间")
    val statistics_period_after: Date? = null

    @ApiModelProperty(value = "原料获取阶段")
    val raw_material_acquisition_stage: Any? = null

    @ApiModelProperty(value = "生产阶段")
    val production_stage: Any? = null

    @ApiModelProperty(value = "包装阶段")
    val packing_stage: Any? = null

    @ApiModelProperty(value = "销售阶段")
    val sale_stage: Any? = null

    @ApiModelProperty(value = "使用阶段")
    val use_stage: Any? = null

    @ApiModelProperty(value = "销毁阶段")
    val disuse_stage: Any? = null
}
