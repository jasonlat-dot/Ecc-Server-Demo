package io.github.jasonlat.app.config;

import cc.jq1024.middleware.redisson.IRedissonService;
import cc.jq1024.middleware.token.service.ITokenService;
import io.github.jasonlat.authenticate.model.entity.UserPublicKey;
import io.github.jasonlat.authenticate.service.AuthenticateService;
import io.github.jasonlat.middleware.domain.model.entity.UserPublicData;
import io.github.jasonlat.middleware.domain.service.EccUserDataService;
import io.github.jasonlat.types.security.constants.SecurityConstant;
import io.github.jasonlat.types.utils.Constant;
import io.github.jasonlat.types.utils.StringUtils;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.http.HttpServletRequest;

@Configuration
public class CustomEccConfiguration {

    private static final Logger log = LoggerFactory.getLogger(CustomEccConfiguration.class);

    private final AuthenticateService authenticateService;
    private final HttpServletRequest httpServletRequest;
    private final IRedissonService redissonService;
    private final ITokenService tokenService;
    public CustomEccConfiguration(AuthenticateService authenticateService, HttpServletRequest httpServletRequest, IRedissonService redissonService, ITokenService tokenService) {
        this.authenticateService = authenticateService;
        this.httpServletRequest = httpServletRequest;
        this.redissonService = redissonService;
        this.tokenService = tokenService;
    }

    @Bean
    public EccUserDataService eccUserDataService() {
        return new EccUserDataService() {
            @Override
            public UserPublicData loadUserPublicData(String user) {

                UserPublicKey userPublicKey = authenticateService.queryUserPublicKey(user);
                if (userPublicKey == null) {
                    return null;
                }
                return new UserPublicData(userPublicKey.getUserPublicX(), userPublicKey.getUserPublicY());
            }

            @Override
            public String getCurrentUserId() {
                log.info("getCurrentUserId");
                // 0. 优先从 security 中获取
                try {
                    String user = SecurityContextHolder.getContext().getAuthentication().getName();
                    if (StringUtils.isNotEmpty(user) && !"anonymousUser".contains(user)) {
                        return user;
                    }
                } catch (Exception e) {
                    log.warn("getCurrentUserId from securityContext failed", e);
                }
                // 1. 获取请求头
                String userToken = httpServletRequest.getHeader(SecurityConstant.TOKEN_HEADER);
                if (StringUtils.isNotEmpty(userToken)) {
                    // 解析 token
                    String simpleUUID = userToken.replace("Bearer ", "");
                    String token = redissonService.getValue(simpleUUID);
                    return tokenService.decode(token).getSubject();
                }
                // throw new IllegalArgumentException("getCurrentUserId failed");
                return null;
            }
        };
    }
}
