package org.sangonomiya.app.entity;

import com.alibaba.fastjson2.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 产品对象
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "Product对象", description = "产品对象")
public class Product {

    private static boolean CRADLE_TO_GATE = false;
    private static boolean CRADLE_TO_GRAVE = true;

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty("产品ID")
    private Integer id;

    @TableField("name")
    @ApiModelProperty("产品名称")
    private String name;

    @TableField("type")
    @ApiModelProperty("产品类型")
    private String type;

    @TableField("model")
    @ApiModelProperty("产品型号")
    private String model;

    @TableField("amount")
    @ApiModelProperty("产品数量")
    private Integer amount;

    @TableField("unit")
    @ApiModelProperty("产品单位")
    private String unit;

    @TableField("weight")
    @ApiModelProperty("产品重量")
    private Float weight;

    @TableField("lifeCycleType")
    @ApiModelProperty("生命周期类型")
    private Boolean lifeCycleType;

    @TableField("pic")
    @ApiModelProperty("产品图片地址")
    private String pic;

    @TableField("statisticsPeriodBefore")
    @ApiModelProperty("产品统计开始时间")
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date statisticsPeriodBefore;

    @TableField("statisticsPeriodAfter")
    @ApiModelProperty("产品统计终止时间")
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date statisticsPeriodAfter;

    @TableField("tag")
    @ApiModelProperty("NFC标签")
    private String tag;

}
