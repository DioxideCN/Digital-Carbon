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
 * @author Dioxide.CN
 * @date 2023/3/21 13:56
 * @since 1.0
 */
@Getter
@Setter
@TableName("repo_message")
@Accessors(chain = true)
@ApiModel(value = "Notification对象", description = "消息记录")
public class Notification implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("通知ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("接受者ID")
    @TableField("receiver")
    private Integer receiver;

    @ApiModelProperty("发送人ID")
    @TableField("sender")
    private Integer sender;

    @ApiModelProperty("消息JSON")
    @TableField("message")
    private String message;

    @ApiModelProperty("发送时间")
    @TableField("create_time")
    private String createTime;

    @ApiModelProperty("是否已读")
    @TableField("is_read")
    private boolean isRead;

    @TableField(exist = false)
    private String senderUsername;
    @TableField(exist = false)
    private String receiverUsername;

}
