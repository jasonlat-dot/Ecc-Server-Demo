package io.github.jasonlat.authenticate.model.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 用户信息实体
 * @author li--jiaqiang 2024−12−25
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * token
     */
    private String token;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 密码
     */
    private String password;

    /**
     * 角色类型
     */
    private String userType;


    /**
     * 头像云存储地址
     */
    private String avatarUrl;

    /**
     * 上次登录时间
     */
    private Date lastLoginTime;
}