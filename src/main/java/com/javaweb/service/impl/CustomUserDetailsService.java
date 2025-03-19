package com.javaweb.service.impl;

import com.javaweb.entity.User;
import com.javaweb.enums.AccountStatus;
import com.javaweb.model.dto.MyUserDetail;
import com.javaweb.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User userEntity = userRepository.findOneByUsernameAndAccountStatus(username, AccountStatus.ACTIVE);
        if (userEntity == null) {
            throw new UsernameNotFoundException("Username not found");
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        userEntity.getRoles()
                .forEach(role ->
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getCode()))
        );

        MyUserDetail myUserDetail = new MyUserDetail(
                username,
                userEntity.getPassword(),
                true, true, true, true,
                authorities
        );

        // Copy remaining properties
        myUserDetail.setId(userEntity.getId());
        myUserDetail.setFullName(userEntity.getFullName());


        return myUserDetail;
    }
}