package io.github.jasonlat.types.security.util;

import cc.jq1024.middleware.redisson.IRedissonService;
import io.github.jasonlat.types.security.constants.SecurityConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author li--jiaqiang 2025−02−28
 */
@Component
public class CacheService {

    private final static String AUTHORIZATION_KEY = SecurityConstant.TOKEN_KEY + SecurityConstant.UNDER_LINE;
    private static final Logger log = LoggerFactory.getLogger(CacheService.class);

    private final IRedissonService redissonService;
    public CacheService(IRedissonService redissonService) {
        this.redissonService = redissonService;
    }

    /**
     * 泛型的getter方法
     */
    public <T> T getAuthorizationWithUsername(String key) {
        return this.getAuthorization(AUTHORIZATION_KEY + key);
    }

    public <T> T getAuthorization(String key) {
        return redissonService.getValue(key);
    }

    public <T> T getAuthorizationWithUsername(String key, Class<T> clazz)  {
        return this.getAuthorization(AUTHORIZATION_KEY + key, clazz);
    }
    public <T> T getAuthorization(String key, Class<T> clazz)  {
        return redissonService.getValue(key, clazz);
    }

    /**
     * 泛型的set方法
     */
    public <T> void setAuthorizationWithUsername(String key, T value) {
        this.setAuthorization(AUTHORIZATION_KEY + key, value);
    }
    public <T> void setAuthorization(String key, T value) {
        this.setAuthorization(key, value, -1L);
    }

    public <T> void setAuthorizationWithUsername(String key, T value, long time) {
        this.setAuthorization(AUTHORIZATION_KEY + key, value, time);
    }
    public <T> void setAuthorization(String key, T value, long time) {
        this.setAuthorization(key, value, time, TimeUnit.MICROSECONDS);
    }

    public <T> void setAuthorizationWithUsername(String key, T value, long time, TimeUnit timeUnit) {
        this.setAuthorization(AUTHORIZATION_KEY + key, value, time, timeUnit);
    }
    public <T> void setAuthorization(String key, T value, long time, TimeUnit timeUnit) {
        log.info("setAuthorization key: {}, value: {}", key, value);
        redissonService.setValue(key, value, time, timeUnit);
    }
}