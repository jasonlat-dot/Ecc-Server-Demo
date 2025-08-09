package io.github.jasonlat.app.security.handler;

import cc.jq1024.middleware.token.service.ITokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.jasonlat.types.model.Response;
import io.github.jasonlat.types.security.constants.SecurityConstant;
import io.github.jasonlat.types.security.model.SecurityUser;
import io.github.jasonlat.types.security.util.CacheService;
import io.github.jasonlat.types.utils.ResponseUtil;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * 登录成功处理器
 *
 * @author star
 */
@Component
@AllArgsConstructor
public class SecurityAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private static final Logger log = LoggerFactory.getLogger(SecurityAuthenticationSuccessHandler.class);
    private final CacheService cacheService;
    private final ObjectMapper objectMapper;
    private final ITokenService tokenService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        cacheService.setAuthorizationWithUsername(authentication.getName(), authentication.getPrincipal(), SecurityConstant.TOKEN_EXPIRE, TimeUnit.SECONDS);
        SecurityUser securityUser = cacheService.getAuthorizationWithUsername(authentication.getName());
        log.info("{} 登录成功： authentication.getName() = {}", securityUser, authentication.getName());
        ResponseUtil.writeJson(response,
                objectMapper.writeValueAsString(Response.ok(tokenService.createJWT(new HashMap<String, Object>() {
                    private static final long serialVersionUID = 1L;
                    {
                        put("userId", authentication.getName());
                    }
                }, authentication.getName()), "登录成功")));
    }

}
