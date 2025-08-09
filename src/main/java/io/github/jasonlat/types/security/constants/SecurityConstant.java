package io.github.jasonlat.types.security.constants;

/**
 * Security相关的常量
 *
 * @author star
 */
public class SecurityConstant {
    /**
     * jwt token 在 redis 中存储的key
     */
    public static final String TOKEN_KEY = "Authorization";
    /**
     * jwt token 在 请求头中的key
     */
    public static final String TOKEN_HEADER = "Authorization";
    /**
     * jwt token 在 redis 中存储的过期时间，单位秒
     */
    public static final long TOKEN_EXPIRE = 7 * 24 * 60 * 60L;

    public static final String PREFIX = "Bearer ";

    /**
     * 下划线
     */
    public static final String UNDER_LINE = "_";
}
