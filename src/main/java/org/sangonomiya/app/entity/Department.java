package org.sangonomiya.app.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.sangonomiya.app.core.ListHandler;

import javax.annotation.Nullable;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 部门管理
 * @author Dioxide.CN
 * @date 2023/2/28 14:42
 * @since 1.0
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@ApiModel(value = "Department对象", description = "部门对象")
@TableName(value = "repo_department", autoResultMap = true)
public class Department implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("部门ID")
    @TableId(value = "id", type = IdType.AUTO)
    private int id;

    @ApiModelProperty("部门名称")
    @TableField(value = "department_name")
    private String departmentName;

    @ApiModelProperty("子部门ID")
    @TableField(value = "children_id", typeHandler = ListHandler.class)
    private List<String> children;

    @ApiModelProperty("父部门ID")
    @TableField(value = "parent_id",
            updateStrategy = FieldStrategy.IGNORED,
            insertStrategy = FieldStrategy.IGNORED)
    @Nullable
    private Integer parent;

    @ApiModelProperty("创建人ID")
    @TableField(value = "creator_id")
    private Integer creator;

    @ApiModelProperty("负责人ID")
    @TableField(value = "manager_id")
    private Integer manager;

    @ApiModelProperty("创建时间")
    @TableField(value = "create_time")
    private String createTime;

    @TableField(exist = false)
    private String creatorName;

    @TableField(exist = false)
    private String managerName;

}
