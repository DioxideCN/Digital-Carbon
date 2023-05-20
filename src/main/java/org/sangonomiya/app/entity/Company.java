package org.sangonomiya.app.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
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
@TableName("repo_company")
@Accessors(chain = true)
@ApiModel(value = "Company对象", description = "企业对象")
public class Company implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("企业ID")
    @TableId(value = "id", type = IdType.AUTO)
    public Integer id;

    @ApiModelProperty("企业名称")
    @TableField(value = "company_name")
    private String companyName;

    @ApiModelProperty("公司成员数量")
    @TableField("company_user_num")
    private Integer companyUserNum;

    @ApiModelProperty("企业所属区域")
    @TableField(value = "company_location")
    private String companyLocation;

    @ApiModelProperty("企业账户创建时间")
    @TableField(value = "company_create_time")
    private String companyCreateTime;

    @ApiModelProperty("企业行业类型")
    @TableField(value = "company_industry_type")
    private String companyIndustryType;

}
