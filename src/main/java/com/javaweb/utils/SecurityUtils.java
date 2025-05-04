package com.javaweb.utils;

import com.javaweb.model.dto.MyUserDetail;
import com.javaweb.model.dto.UserDTO;
import com.javaweb.view.user.UserSessionManager;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Set;

@Getter
@Setter
public class SecurityUtils {

    private static Set<String> authorities;

    public static MyUserDetail getPrincipal() {
        // First try to get from security context (for server-side code)
        Object principal = null;
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        }

        if (principal instanceof MyUserDetail) {
            return (MyUserDetail) principal;
        }

        // If not found, create a MyUserDetail from the UserSessionManager
        // for client-side code using token-based auth
        if (UserSessionManager.getInstance().isLoggedIn()) {
            UserDTO user = UserSessionManager.getInstance().getCurrentUser();
            return new MyUserDetail(
                    user.getUsername(),
                    "", // Password not needed here
                    true, true, true, true,
                    Collections.emptyList()
            ) {{
                setId(user.getId());
                setFullName(user.getFullName());
            }};
        }

        return null;
    }
}