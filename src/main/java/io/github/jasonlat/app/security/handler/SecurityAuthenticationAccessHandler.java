package io.github.jasonlat.app.security.handler;

import lombok.AllArgsConstructor;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * 自定义权限校验实例
 * @author star
 */
@Component
@AllArgsConstructor
public class SecurityAuthenticationAccessHandler implements AuthorizationManager<HttpServletRequest> {

    private final AntPathMatcher antPathMatcher;

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authenticationSupplier, HttpServletRequest request) {
        // 步骤 1: 从 Supplier 中获取 Authentication 对象
        Authentication authentication = authenticationSupplier.get();
        // 步骤 2: 从 Authentication 对象中获取用户的权限集合
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        boolean isAuthorized = isAuthorized(request, authorities);
        // 步骤 6: 根据匹配结果创建 AuthorizationDecision 对象
        return new AuthorizationDecision(isAuthorized);
    }

    private boolean isAuthorized(HttpServletRequest request, Collection<? extends GrantedAuthority> authorities) {
        // 步骤 3: 将权限集合转换为 Stream
        Stream<? extends GrantedAuthority> authorityStream = authorities.stream();
        // 步骤 4: 定义一个 Predicate 用于检查权限是否与请求的 URI 匹配
        Predicate<GrantedAuthority> authorityMatcher = grantedAuthority -> {
            String authority = grantedAuthority.getAuthority();
            String requestUri = request.getRequestURI();
            return antPathMatcher.match(authority, requestUri);
        };
        // 步骤 5: 使用 anyMatch 方法检查是否有任何一个权限与请求的 URI 匹配
        return authorityStream.anyMatch(authorityMatcher);
    }
}
