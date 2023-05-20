package org.sangonomiya.app.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 公司对象，Mybatis-plus将以该对象的形式返回CRUD结果
 * @author Dioxide.CN
 * @date 2023/2/28 15:58
 * @since 1.0
 */
@Getter
@Setter
@TableName("repo_payment")
@Accessors(chain = true)
@ApiModel(value = "Payment对象", description = "存储用户套餐订单记录")
public class Payment implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("订单ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("所属账户ID")
    @TableField("hold_user_id")
    private Integer holdUserId;

    @ApiModelProperty("套餐结束日期")
    @TableField(value = "ending_date")
    private String endingDate;

}
