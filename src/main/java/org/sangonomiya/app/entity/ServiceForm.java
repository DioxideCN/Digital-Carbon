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
@TableName("repo_service_form")
@Accessors(chain = true)
@ApiModel(value = "ServiceForm对象", description = "企业一对一咨询表对象")
public class ServiceForm implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("表ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("企业名称")
    @TableField(value = "company_name")
    private String companyName;

    @ApiModelProperty("所属行业")
    @TableField("industry_type")
    private String industryType;

    @ApiModelProperty("所属地区")
    @TableField(value = "region_location")
    private String regionLocation;

    @ApiModelProperty("企业需求")
    @TableField(value = "company_needs")
    private String companyNeeds;

    @ApiModelProperty("提交时间戳")
    @TableField(value = "postTimestamp")
    private String post_timestamp;

}
