package io.github.jasonlat.types.utils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * HttpServletResponse 统一返回结果工具类
 *
 * @author star
 */
public class ResponseUtil {
    /**
     * 分会JSON格式的字符串
     */
    public static void writeJson(HttpServletResponse response, String jsonContent) {
        response.setContentType("application/json;charset=UTF-8");
        try {
            response.getWriter().write(jsonContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
