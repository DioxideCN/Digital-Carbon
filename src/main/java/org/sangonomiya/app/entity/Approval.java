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
@TableName("repo_approval")
@Accessors(chain = true)
@ApiModel(value = "Approval对象", description = "OA审批表对象")
public class Approval implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("OA审批表ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("审批表标题")
    @TableField(value = "title")
    private String title;

    @ApiModelProperty("审批表紧急级别")
    @TableField(value = "emergency_level")
    private Integer emergencyLevel;

    @ApiModelProperty("审批表内容")
    @TableField("content")
    private String content;

    @ApiModelProperty("审批表状态")
    @TableField(value = "state")
    private Integer state;

    @ApiModelProperty("审批表创建时间")
    @TableField(value = "create_time")
    private String createTime;

}
