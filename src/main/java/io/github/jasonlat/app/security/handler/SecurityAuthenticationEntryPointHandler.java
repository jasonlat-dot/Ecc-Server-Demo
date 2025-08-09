package io.github.jasonlat.app.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.jasonlat.types.enums.ResponseCode;
import io.github.jasonlat.types.model.Response;
import io.github.jasonlat.types.utils.ResponseUtil;
import lombok.AllArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 匿名用户访问无权限处理器
 *
 * @author star
 */
@Component
@AllArgsConstructor
public class SecurityAuthenticationEntryPointHandler implements AuthenticationEntryPoint {
    private ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        ResponseUtil.writeJson(response, objectMapper.writeValueAsString(Response.error(ResponseCode.CLIENT_A0300)));
    }
}
