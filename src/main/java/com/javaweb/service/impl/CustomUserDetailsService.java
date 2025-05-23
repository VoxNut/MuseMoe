package com.javaweb.service.impl;

import com.javaweb.entity.UserEntity;
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

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.findOneByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }

        // Convert role entities to GrantedAuthority
        Collection<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getCode().name()))
                .collect(Collectors.toList());

        // Create user details with all necessary information
        MyUserDetail userDetails = new MyUserDetail(
                user.getUsername(),
                user.getPassword(),
                AccountStatus.ACTIVE.equals(user.getAccountStatus()),
                true, true, true,
                authorities
        );

        // Set additional properties
        userDetails.setId(user.getId());
        userDetails.setFullName(user.getFullName());
        userDetails.setEmail(user.getEmail());
        userDetails.setAccountStatus(user.getAccountStatus());
        if (user.getArtist() != null) {
            userDetails.setArtistId(user.getArtist().getId());
        }

        // Set avatar ID if available
        if (user.getAvatar() != null) {
            userDetails.setAvatarId(user.getAvatar().getGoogleDriveId());
        }

        return userDetails;
    }
}