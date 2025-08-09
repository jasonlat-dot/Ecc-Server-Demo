package io.github.jasonlat.app.security.handler;

import io.github.jasonlat.types.enums.ResponseCode;
import io.github.jasonlat.types.model.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller中统一异常处理
 *
 * @author star
 */
@Slf4j
@ResponseBody
@ControllerAdvice
public class ControllerExceptionHandler {
    /**
     * 通用异常处理
     *
     * @param e 异常对象
     */
    @ExceptionHandler(Exception.class)
    public Response<Object> exceptionHandler(Exception e) {
        e.printStackTrace();
        log.error(e.getMessage());
        return Response.error();
    }

    @ExceptionHandler(AccessDeniedException.class)
    public Response<Object> accessDeniedExceptionHandler(AccessDeniedException e) {
        log.error(e.getMessage());
        return Response.error(ResponseCode.CLIENT_A0304);
    }

}
