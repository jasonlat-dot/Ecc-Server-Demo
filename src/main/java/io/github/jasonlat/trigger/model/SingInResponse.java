package io.github.jasonlat.trigger.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * 登录结果返回
 * @author li--jiaqiang 2024−12−23
 */
@Data
@Builder
@AllArgsConstructor
public class SingInResponse {

    /**
     * token
     */
    private final String token;

    /**
     * 头像云存储地址
     */
    private String avatarUrl;

    /**
     * 邮箱
     */
    private String email;
}