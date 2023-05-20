package org.sangonomiya.app.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@ApiModel(value = "LifeCycle对象", description = "生命周期对象")
public class LifeCycle {

    @TableField("id")
    @ApiModelProperty("生命周期(产品)ID")
    private Integer id;

    @TableField("raw_material_acquisition_stage")
    @ApiModelProperty("原料获取阶段")
    private String rawMaterialAcquisitionStage;

    @TableField("production_stage")
    @ApiModelProperty("生产阶段")
    private String productionStage;

    @TableField("packing_stage")
    @ApiModelProperty("打包阶段")
    private String packingStage;

    @TableField("sale_stage")
    @ApiModelProperty("销售阶段")
    private String saleStage;

    @TableField("use_stage")
    @ApiModelProperty("使用阶段")
    private String useStage;

    @TableField("disuse_stage")
    @ApiModelProperty("废弃阶段")
    private String disuseStage;

}
