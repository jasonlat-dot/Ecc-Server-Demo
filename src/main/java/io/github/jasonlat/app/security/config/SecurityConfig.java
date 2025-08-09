package io.github.jasonlat.app.security.config;

import cn.hutool.core.util.ObjectUtil;
import io.github.jasonlat.app.security.filter.SecurityJwtAuthenticationFilter;
import io.github.jasonlat.app.security.handler.*;
import io.github.jasonlat.authenticate.model.entity.UserEntity;
import io.github.jasonlat.authenticate.repository.IAuthenticateRepository;
import io.github.jasonlat.types.security.model.SecurityUser;
import io.github.jasonlat.types.utils.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.*;

/**
 * Security 配置
 *
 * @author star
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    public static final String[] WHITE_LIST = new String[]{//这里可避开错误，凭证不能为空
            "/api/v1/static/**", "/error",
            "/public/**",
            "/api/v1" + Constant.RESOURCE_PREFIX + "/**",
            "/api/v1/actuator/health",
            "/api/v1/actuator/info",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/api/v1/user/authenticate/login",
            "/api/v1/user/authenticate/code",
            "/api/v1/user/authenticate/test",
            "/api/v1/test/2",
    };

    private final IAuthenticateRepository userRepository;
    public SecurityConfig(IAuthenticateRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * SpringSecurity 跨域配置
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Collections.singletonList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Collections.singletonList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * 路径匹配方式
     */
    @Bean
    public AntPathMatcher antPathMatcher() {
        return new AntPathMatcher();
    }

    /**
     * 配置Security加密方式
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 配置Security授权规则
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            SecurityAuthenticationSuccessHandler securityAuthenticationSuccessHandler,
            SecurityAuthenticationFailureHandler securityAuthenticationFailureHandler,
            SecurityAuthenticationAccessHandler securityAuthenticationAccessHandler,
            SecurityAuthenticationEntryPointHandler securityAuthenticationEntryPointHandler,
            SecurityLogoutHandler securityLogoutHandler,
            SecurityLogoutSuccessHandler securityLogoutSuccessHandler,
            SecurityAccessDeniedHandler securityAccessDeniedHandler,
            SecurityJwtAuthenticationFilter securityJwtAuthenticationFilter,
            CorsConfigurationSource corsConfigurationSource
    ) throws Exception {
        // 关闭表单的防伪造请求（在表单中默认携带一个随机token字符串），因为前后端分离没办法保证token
        http.csrf().disable();

        // 开启和配置表单登录方式 - /** 禁用登录，自己实现 */
//        http.formLogin().disable();
        http.formLogin(customizer -> customizer
                // 配置表单登录成功后的处理方法
                .loginProcessingUrl("/user/login")
                .successHandler(securityAuthenticationSuccessHandler)
                // 配置表单登录失败后的处理方法
                .failureHandler(securityAuthenticationFailureHandler)
                .permitAll()
        );

        http.authorizeHttpRequests(authorize -> authorize
                // 白名单资源
                .requestMatchers(getRequestMatchers())
                        .permitAll()
                        .anyRequest().permitAll()
                // 其他请求需认证(不需要授权，但是需要认证)
                // .anyRequest().authenticated()
                // .anyRequest().access((authentication, request) -> securityAuthenticationAccessHandler.check(authentication, request.getRequest()))
        );


        // 退出策略
        http.logout(customizer -> customizer
                .logoutUrl("logout") // 显式指定退出端点（可选）
                .addLogoutHandler(securityLogoutHandler)
                .logoutSuccessHandler(securityLogoutSuccessHandler)
                .clearAuthentication(true) // 默认已启用，确保清理认证信息
                .invalidateHttpSession(true) // 默认已启用，使 Session 失效
                .permitAll()
        );
        // 异常处理
        http.exceptionHandling(customizer -> customizer
                .authenticationEntryPoint(securityAuthenticationEntryPointHandler)
                .accessDeniedHandler(securityAccessDeniedHandler)
        );

        // 添加自定义认证过滤器
        http.addFilterAfter(
                securityJwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class
        );
        // Session管理策略
        http.sessionManagement(customizer -> customizer
                .sessionCreationPolicy(SessionCreationPolicy.NEVER)
        );
        // 跨域配置
        http.cors().configurationSource(corsConfigurationSource);
        // 构造出SecurityFilterChain对象并返回
        return http.build();
    }

    /**
     * 自定义实现的 AuthenticationProvider
     */
    @Bean
    public UserDetailsService userDetailsService() {
        // 自定义 UserDetailsService
        return userId -> {
            try {
                UserEntity userEntity = userRepository.queryUserInfoByUserId(userId);
                if (ObjectUtil.isEmpty(userEntity)) {
                    throw new UsernameNotFoundException("用户不存在");
                }
                LinkedList<String> authorities = new LinkedList<>();
                // 2、加载数据库中角色信息
                String userType = userEntity.getUserType();
                if (!ObjectUtil.isEmpty(userType)) {
                    String[] roles = userType.split(",");
                    for (String roleType : roles) {
                        if (Objects.nonNull(roleType)) {
                            authorities.add("ROLE_" + roleType);
                        }
                    }
                }
                log.info("用户权限列表：{}", authorities);
                // 使用用户信息、角色列表、权限列表构建出UserDetails对象
                return new SecurityUser(userId, userEntity.getPassword(), authorities);
            } finally {
                log.info("用户:{}登录执行 loadUserByUsername() 方法完成。",userId);
            }
        };
    }

    /**
     * 将 String 数组转为 RequestMatcher 数组
     */
    private RequestMatcher[] getRequestMatchers() {
        List<RequestMatcher> matchers = new ArrayList<>();
        for (String string : SecurityConfig.WHITE_LIST) {
            AntPathRequestMatcher antPathRequestMatcher = new AntPathRequestMatcher(string);
            matchers.add(antPathRequestMatcher);
        }
        RequestMatcher[] array = matchers.toArray(new RequestMatcher[0]);
        System.out.println();
        System.out.println("白名单列表："+ Arrays.toString(array));
        System.out.println();
        return array;
    }
}
