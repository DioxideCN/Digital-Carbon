package org.sangonomiya.app.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * 用户权限组
 * @author Dioxide.CN
 * @date 2023/2/28 14:34
 * @since 1.0
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("repo_permission_group")
@ApiModel(value = "Permission对象", description = "权限组对象与归属于公司所有与用户绑定")
public class Permission {

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty("权限组ID")
    private int id;

    @ApiModelProperty("权限组名称")
    @TableField("group_name")
    private String groupName;

    @ApiModelProperty("权限列表")
    private String permissions;

    @ApiModelProperty("权限组备注")
    @TableField("remark")
    private String remark;

    @ApiModelProperty("创建时间")
    @TableField("create_time")
    private String createTime;

    @ApiModelProperty("创建时间")
    @TableField("create_user_id")
    private Integer createUserId;

    @TableField(exist = false)
    private UserVO createUser;

}
