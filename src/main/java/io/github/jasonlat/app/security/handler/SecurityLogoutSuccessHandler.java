package io.github.jasonlat.app.security.handler;

import cc.jq1024.middleware.redisson.IRedissonService;
import cc.jq1024.middleware.token.service.ITokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.jasonlat.types.enums.ResponseCode;
import io.github.jasonlat.types.model.Response;
import io.github.jasonlat.types.security.constants.SecurityConstant;
import io.github.jasonlat.types.utils.ResponseUtil;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * 退出成功处理器
 *
 * @author star
 */
@Component
@AllArgsConstructor
public class SecurityLogoutSuccessHandler implements LogoutSuccessHandler {
    private static final Logger log = LoggerFactory.getLogger(SecurityLogoutSuccessHandler.class);
    private ITokenService tokenService;
    private IRedissonService redissonService;
    private ObjectMapper objectMapper;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // 清除Session（前后端不分离）
        HttpSession session = request.getSession();
        if (session != null) {
            session.invalidate();
        }
        try {
            String jwt = request.getHeader(SecurityConstant.TOKEN_HEADER);
            String token = jwt.replace("Bearer ", "");
            String name = tokenService.decode(token).getSubject();
            // 清除Redis（前后端分离）
            redissonService.setValue(SecurityConstant.TOKEN_KEY + SecurityConstant.UNDER_LINE + name, authentication, 0);
            ResponseUtil.writeJson(response, objectMapper.writeValueAsString(Response.ok(null,"退出成功")));
        } catch (Exception e) {
            ResponseUtil.writeJson(response, objectMapper.writeValueAsString(Response.error(ResponseCode.CLIENT_A0304)));
        } finally {
            log.info("退出成功！");
        }
    }
}
