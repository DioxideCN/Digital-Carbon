package org.sangonomiya.app.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;

/**
 * @author Dioxide.CN
 * @date 2023/2/28 20:16
 * @since 1.0
 */
@Getter
@Setter
@ToString
@TableName("repo_user")
@ApiModel(value = "UserVO对象", description = "")
public class UserVO implements Serializable, UserDetails {

    @Serial
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("用户ID")
    @TableId(value = "id", type = IdType.AUTO)
    private int id;

    @ApiModelProperty("头像编号")
    private Integer portrait;

    @ApiModelProperty("用户昵称")
    private String username;

    @ApiModelProperty("用户实名")
    private String realname;

    @ApiModelProperty("用户邮箱")
    private String email;

    @ApiModelProperty("用户性别")
    private String gender;

    @ApiModelProperty("用户密码")
    private String password;

    @ApiModelProperty("联系方式")
    private String phone;

    @ApiModelProperty("用户城市")
    private String city;

    @ApiModelProperty("是否启用")
    private boolean enable;

    @ApiModelProperty("注册时间")
    @TableField("create_time")
    private String createTime;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
