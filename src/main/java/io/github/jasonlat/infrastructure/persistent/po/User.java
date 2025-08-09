package io.github.jasonlat.infrastructure.persistent.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

import lombok.*;

/**
 * @author lijiaqiang@ljq1024.cc
 * @since 2025-06-17
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("password")
    private String password;

    @TableField("email")
    private String email;

    /**
     * 应用标识符
     */
    @TableField("appid")
    private String appid;

    @TableField("user_type")
    private String userType;

    @TableField("created_at")
    private Date createdAt;

    @TableField("updated_at")
    private Date updatedAt;

    @TableField("last_login")
    private Date lastLogin;

    @TableField("status")
    private String status;

    @TableField("avatar_url")
    private String avatarUrl;


}
