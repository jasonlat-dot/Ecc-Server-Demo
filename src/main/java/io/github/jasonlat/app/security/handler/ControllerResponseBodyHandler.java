package io.github.jasonlat.app.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.jasonlat.types.model.Response;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * Controller中统一返回结果处理
 *
 * @author star
 */
@ControllerAdvice
@AllArgsConstructor
public class ControllerResponseBodyHandler implements ResponseBodyAdvice<Object> {
    private ObjectMapper objectMapper;

    /**
     * @param returnType    返回值类型
     * @param converterType 消息转换器的类型
     * @return /
     */
    @Override
    public boolean supports(MethodParameter returnType, @NotNull Class converterType) {
        // 如果已经是 Result 包装过的就跳过
        return !(
                returnType.getParameterType().isAssignableFrom(Response.class)
                        // springdoc 的 Controller 不需要拦截，包括如下：
                        // org.springdoc.webmvc.api.OpenApiWebMvcResource
                        // org.springdoc.webmvc.ui.SwaggerConfigResource
                        || returnType.getDeclaringClass().getName().contains("springdoc")
                // 如果标注有 @IgnoreResponseBody 注解的也跳过
                // || return !returnType.hasMethodAnnotation(IgnoreResponseBody.class);
        );
    }

    @SneakyThrows
    @Override
    public Object beforeBodyWrite(Object body, @NotNull MethodParameter returnType, @NotNull MediaType selectedContentType, @NotNull Class selectedConverterType, @NotNull ServerHttpRequest request, @NotNull ServerHttpResponse response) {
        if (body instanceof Response) {
            return body;
        } else if (body instanceof String) {
            return objectMapper.writeValueAsString(Response.ok(null, (String) body));
        }
        return Response.ok(body);
    }
}
