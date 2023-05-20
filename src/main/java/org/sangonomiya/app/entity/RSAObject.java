package org.sangonomiya.app.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.annotation.Nullable;
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
@NoArgsConstructor
@AllArgsConstructor
@TableName("repo_application")
@Accessors(chain = true)
@ApiModel(value = "RSAObject对象", description = "RSAObject秘钥对")
public class RSAObject implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("企业ID")
    @TableField(value = "company_id")
    private Integer companyId;

    @ApiModelProperty("应用名称")
    @TableField(value = "title")
    private String title;

    @ApiModelProperty("应用图标ID")
    @TableField(value = "img_id")
    private int imgId;

    @ApiModelProperty("应用ID")
    @TableField(value = "app_id")
    private String appId;

    @ApiModelProperty("应用公钥")
    @TableField(value = "app_public_key")
    private String appPublicKey;

    @ApiModelProperty("企业公钥")
    @TableField("com_public_key")
    private String comPublicKey;

    @ApiModelProperty("企业私钥")
    @TableField(value = "com_private_key")
    private String comPrivateKey;

    @ApiModelProperty("应用备注")
    @TableField(value = "remark")
    private String remark;

    @ApiModelProperty("回调地址")
    @TableField(value = "notify_url")
    private String notifyUrl;

    @ApiModelProperty("开发状态")
    @TableField(value = "enable")
    private boolean enable;

    @ApiModelProperty("创建时间")
    @TableField(value = "create_time")
    private String createTime;

    @ApiModelProperty("启用能力ID")
    @TableField(value = "ability_id")
    @Nullable
    private Integer abilityId;

    @ApiModelProperty("绑定产品ID")
    @TableField(value = "binding_product_id")
    @Nullable
    private String bindingProductId;
}
