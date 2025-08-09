package io.github.jasonlat.app.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.jasonlat.types.enums.ResponseCode;
import io.github.jasonlat.types.model.Response;
import io.github.jasonlat.types.utils.ResponseUtil;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 登录失败处理器，一般是用户密码错误
 *
 * @author star
 */
@Component
@AllArgsConstructor
public class SecurityAuthenticationFailureHandler implements AuthenticationFailureHandler {
    private static final Logger log = LoggerFactory.getLogger(SecurityAuthenticationFailureHandler.class);
    private ObjectMapper objectMapper;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        log.info("登录失败");
        ResponseUtil.writeJson(response, objectMapper.writeValueAsString(Response.error(ResponseCode.CLIENT_A0212)));
    }
}
