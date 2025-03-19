package com.javaweb.utils;

import com.javaweb.model.dto.MyUserDetail;
import lombok.Setter;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashSet;
import java.util.Set;

public class SecurityUtils {
    @Setter
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

    public static Set<String> getAuthorities() {
        return authorities != null ? authorities : new HashSet<>();
    }
}