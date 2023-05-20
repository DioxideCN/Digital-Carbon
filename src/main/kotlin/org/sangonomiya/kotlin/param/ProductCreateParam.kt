package org.sangonomiya.kotlin.param

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.util.*

/**
 *
 * @author Dioxide.CN
 * @date 2023/4/8 17:01
 * @since 1.0
 */
@ApiModel(value = "ProductCreate参数对象", description = "创建产品时需要传递该对象中的所有参数")
class ProductCreateParam {

    @ApiModelProperty(value = "产品名称", required = true)
    val product_name: String? = null

    @ApiModelProperty(value = "产品类型", required = true)
    val product_type: String? = null

    @ApiModelProperty(value = "产品型号", required = true)
    val product_model: String? = null

    @ApiModelProperty(value = "数量", required = true)
    val product_amount: Int? = null

    @ApiModelProperty(value = "单位", required = true)
    val product_unit: String? = null

    @ApiModelProperty(value = "产品重量", required = true)
    val product_weight: Float? = null

    @ApiModelProperty(value = "生命周期类型", required = true)
    val product_lifecycle: Boolean? = null

    @ApiModelProperty(value = "产品图片地址", required = true)
    val product_pic: String? = null

    @ApiModelProperty(value = "产品统计开始时间", required = true)
    val statistics_period_before: Date? = null

    @ApiModelProperty(value = "产品统计终止时间", required = true)
    val statistics_period_after: Date? = null

    @ApiModelProperty(value = "原料获取阶段", required = true)
    val raw_material_acquisition_stage: Any? = null

    @ApiModelProperty(value = "生产阶段", required = true)
    val production_stage: Any? = null

    @ApiModelProperty(value = "包装阶段", required = true)
    val packing_stage: Any? = null

    @ApiModelProperty(value = "销售阶段", required = true)
    val sale_stage: Any? = null

    @ApiModelProperty(value = "使用阶段", required = true)
    val use_stage: Any? = null

    @ApiModelProperty(value = "销毁阶段", required = true)
    val disuse_stage: Any? = null

}
