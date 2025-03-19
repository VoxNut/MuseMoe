package com.javaweb.converter;

import com.javaweb.builder.UserSearchBuilder;
import com.javaweb.utils.MapUtil;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class UserSearchBuilderConverter {
    public UserSearchBuilder toUserSearchBuilder(Map<String, Object> params, Set<String> roles) {
        return new UserSearchBuilder.Builder()
                .setFullName(MapUtil.getObject(params, "fullname", String.class))
                .setRole(roles)
                .setStatus(MapUtil.getObject(params, "status", String.class))
                .build();
    }
}
