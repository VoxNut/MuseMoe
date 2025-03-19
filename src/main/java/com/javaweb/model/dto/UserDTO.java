package com.javaweb.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class UserDTO extends AbstractDTO<UserDTO> implements Comparable<UserDTO> {
    private Long id;
    private String username;
    private String fullName;
    private String password;
    private Integer status;
    private Set<RoleDTO> roles = new HashSet<>();
    private String avatar;
    private String email;
    private String role;
    private String visibleRoles;
    private String phone;

    @Override
    public int compareTo(UserDTO o) {
        if (fullName == null || o.fullName == null) {
            return username.compareTo(o.username);
        }
        int result = this.fullName.toCharArray()[0] - o.fullName.toCharArray()[0];
        if (result == 0) {
            result = this.fullName.length() - o.fullName.length();
            if (result == 0) {
                result = this.fullName.compareTo(o.fullName);
            }
        }
        return result;
    }

    public boolean hasFullName() {
        return fullName != null;
    }
}