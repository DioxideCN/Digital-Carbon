package org.sangonomiya.app.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author Dioxide.CN
 * @date 2023/3/10 8:11
 * @since 1.0
 */
@Getter
@Setter
@TableName("repo_order_record")
@Accessors(chain = true)
@ApiModel(value = "Order对象", description = "存储订单状态记录")
public class Order {

    @ApiModelProperty("所属账户ID")
    @TableField("user_id")
    private Integer userId;

    @ApiModelProperty("订单号")
    @TableField(value = "order_num")
    private String orderNum;

    @ApiModelProperty("订单创建时间")
    @TableField(value = "create_time")
    private String createTime;

    @ApiModelProperty("订单状态")
    @TableField(value = "biz_state")
    private Integer bizState;

    @ApiModelProperty("购买时长")
    @TableField(value = "buy_year")
    private Integer buyYear;

}
