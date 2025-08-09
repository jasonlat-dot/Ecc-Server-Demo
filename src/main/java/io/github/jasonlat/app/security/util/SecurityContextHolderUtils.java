package io.github.jasonlat.app.security.util;

import io.github.jasonlat.types.security.model.SecurityUser;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * @author li--jiaqiang 2025−06−18
 */
public class SecurityContextHolderUtils {

    public static String getCurrentUsername() {

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof SecurityUser) { // 你的自定义UserDetails实现类
            return ((SecurityUser) principal).getUsername();
        } else if (principal instanceof String) {
            return (String) principal;
        }
        return null;
    }

    public static SecurityUser getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof SecurityUser) {
            return (SecurityUser) principal;
        }
        return null;
    }
}