package io.github.jasonlat.app.security.filter;

import cc.jq1024.middleware.redisson.IRedissonService;
import cc.jq1024.middleware.token.service.ITokenService;
import cn.hutool.core.util.ObjUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.jasonlat.app.security.config.SecurityConfig;
import io.github.jasonlat.types.enums.ResponseCode;
import io.github.jasonlat.types.model.Response;
import io.github.jasonlat.types.security.constants.SecurityConstant;
import io.github.jasonlat.types.security.model.SecurityUser;
import io.github.jasonlat.types.security.util.CacheService;
import io.github.jasonlat.types.utils.ResponseUtil;
import io.jsonwebtoken.Claims;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

/**
 * Jwt 权限认证过滤器
 *
 * @author star
 */
@Component
@AllArgsConstructor
public class SecurityJwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    private final ITokenService tokenService;
    private final IRedissonService redissonService;
    private final ObjectMapper objectMapper;
    private final CacheService cacheService;
    private final AntPathMatcher antPathMatcher;

    @Override
    public void doFilterInternal(HttpServletRequest servletRequest, @NotNull HttpServletResponse servletResponse, @NotNull FilterChain filterChain) throws IOException, ServletException {

        // 0. 排除OPTIONS请求
        if ("OPTIONS".equalsIgnoreCase(servletRequest.getMethod())) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        // 1、表示已经认证过
        if (ObjUtil.isNotEmpty(SecurityContextHolder.getContext().getAuthentication())) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        String requestURI = servletRequest.getRequestURI();
        // 2、如果是白名单页则跳过
        if (Arrays.stream(SecurityConfig.WHITE_LIST).anyMatch(white -> antPathMatcher.match(white, requestURI))) {
            filterChain.doFilter(servletRequest, servletResponse);
            log.info("SecurityJwtAuthenticationFilter - 白名单接口放行：{}", requestURI);
            return;
        }

        // 3、获取请求头中的jwt
        String userToken = servletRequest.getHeader(SecurityConstant.TOKEN_HEADER);
        System.out.println("SecurityJwtAuthenticationFilter: " + Thread.currentThread().getId());
        log.info("SecurityJwtAuthenticationFilter - 请求头中的 Token：{} uri: {}", userToken, requestURI);
        if (ObjUtil.isEmpty(userToken)) {
//            ResponseUtil.writeJson(servletResponse, objectMapper.writeValueAsString(Response.error(ResponseCode.CLIENT_A0305)));
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        // 4、解析 JwtToken
        String username;
        try {
            String simpleUUID = userToken.replace("Bearer ", "");
            String token = redissonService.getValue(simpleUUID);
            Claims claims = tokenService.decode(token);
            username = claims.getSubject();
        } catch (Exception e) {
            ResponseUtil.writeJson(servletResponse, objectMapper.writeValueAsString(Response.error(ResponseCode.CLIENT_A0304)));
            return;
        }

        // 5、从 Redis 中取出 authentication
        SecurityUser securityUser = cacheService.getAuthorizationWithUsername(username);
        if (ObjUtil.isEmpty(securityUser)) {
            ResponseUtil.writeJson(servletResponse, objectMapper.writeValueAsString(Response.error(ResponseCode.CLIENT_A0311)));
            return;
        }
        Collection<? extends GrantedAuthority> authorities = securityUser.getAuthorities();
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(securityUser, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(servletRequest, servletResponse);
    }

}
