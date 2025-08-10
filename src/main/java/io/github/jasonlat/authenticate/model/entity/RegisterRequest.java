package io.github.jasonlat.authenticate.model.entity;

import lombok.Data;
import lombok.ToString;

@ToString
@Data
public class RegisterRequest {

    /**
     * 邮箱 - 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 确认密码
     */
    private String confirmPassword;

    /**
     * 验证码
     */
    private String verificationCode;

    /**
     * 用户公钥 x
     */
    private String userPublicX;

    /**
     * 用户公钥 y
     */
    private String userPublicY;

}