package com.javaweb.utils;

import com.javaweb.model.dto.MyUserDetail;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Set;

@Getter
@Setter
public class SecurityUtils {

    private static Set<String> authorities;

    public static MyUserDetail getPrincipal() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof MyUserDetail) {
            return (MyUserDetail) principal;
        } else if (principal instanceof String) {
            return null;
        }

        return null;
    }


}