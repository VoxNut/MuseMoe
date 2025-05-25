package com.javaweb.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.javaweb.enums.AccountStatus;
import com.javaweb.enums.RoleType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class UserDTO extends AbstractDTO implements Comparable<UserDTO> {
    private String username;
    private String password;
    private String fullName;
    private String email;
    private AccountStatus accountStatus;
    private StreamingMediaDTO avatar;
    private Set<String> roles = new HashSet<>();
    private String role;
    private String preferredLanguage;
    private LocalDateTime lastLoginDate;
    private String avatarId;
    private Date tokenExpiration;
    @JsonIgnore
    private BufferedImage avatarImage;
    private Long artistId;
    private Date lastLoginAt;


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


    public String determineUserRole() {
        if (roles.contains(RoleType.ADMIN.name())) {
            return RoleType.ADMIN.getValue();
        } else if (roles.contains(RoleType.ARTIST.name())) {
            return RoleType.ARTIST.getValue();
        } else if (roles.contains(RoleType.PREMIUM.name())) {
            return RoleType.PREMIUM.getValue();
        } else {
            return RoleType.FREE.getValue();
        }
    }
}