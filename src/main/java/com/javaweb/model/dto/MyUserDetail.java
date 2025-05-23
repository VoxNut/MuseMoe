package com.javaweb.model.dto;

import com.javaweb.enums.AccountStatus;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Getter
@Setter
public class MyUserDetail extends User {
    private Long id;
    private String fullName;
    private String email;
    private AccountStatus accountStatus;
    private String avatarId;
    private Long artistId;

    public MyUserDetail(String username, String password, boolean enabled,
                        boolean accountNonExpired, boolean credentialsNonExpired,
                        boolean accountNonLocked,
                        Collection<? extends GrantedAuthority> authorities) {
        super(username, password, enabled, accountNonExpired,
                credentialsNonExpired, accountNonLocked, authorities);
    }
}