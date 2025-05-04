package com.javaweb.client.impl;

import com.javaweb.client.ApiConfig;
import com.javaweb.client.client_service.UserApiClient;
import com.javaweb.enums.RoleType;
import com.javaweb.model.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

@RequiredArgsConstructor
@Slf4j
class UserApiClientImpl implements UserApiClient {
    private final ApiClient apiClient;
    private final UrlEncoder urlEncoder;
    private final ApiConfig apiConfig;


    @Override
    public Boolean updateUserPassword(Long id, String password) {
        try {
            String url = apiConfig.buildUserUrl("/reset_password");
            return apiClient.put(url,
                    UserDTO.builder()
                            .id(id)
                            .password(password)
                            .build()
                    , Boolean.class);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Boolean createNewUser(String username, String password, String email) {
        try {

            String url = apiConfig.buildUserUrl("/register");

            return apiClient.post(url,
                    UserDTO.builder()
                            .username(username)
                            .password(password)
                            .email(email)
                            .build()
                    , Boolean.class);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public UserDTO fetchUserByEmail(String email) {
        try {
            String url = apiConfig.buildUserUrl("/email?email=" + email);
            return apiClient.get(url, UserDTO.class);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Set<UserDTO> fetchAllUsersBaseOnRole(RoleType role) {
        try {
            String url = apiConfig.buildUserUrl("?status=1&roles=" + role);

            Set<UserDTO> users = apiClient.get(url, Set.class);

            if (users != null) {
                return new TreeSet<>(users);
            } else {
                return Collections.emptySet();
            }
        } catch (Exception e) {
            log.error("Error fetching users with role {}", role, e);
            return Collections.emptySet();
        }
    }

    @Override
    public void updateLastLoginTime() {
        try {
            String url = apiConfig.buildUserUrl("/last_login");
            apiClient.put(url,
                    UserDTO.builder()
                            .lastLoginDate(LocalDateTime.now())
                            .build(),
                    Void.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public UserDTO fetchCurrentUser() {
        try {
            String url = apiConfig.buildUserUrl("/me");
            return apiClient.get(url, UserDTO.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public UserDTO fetchUserByUsername(String username) {
        try {
            String encodedUsername = urlEncoder.encode(username);
            String url = apiConfig.buildUserUrl("/username/" + encodedUsername + "/user_dto");
            return apiClient.get(url, UserDTO.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public UserDTO fetchUserById(Long id) {
        try {
            String url = apiConfig.buildUserUrl("/" + id + "/user_dto");
            return apiClient.get(url, UserDTO.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public UserDTO fetchUserByPhone(String phone) {
        try {
            String encodedPhone = urlEncoder.encode(phone);
            String url = apiConfig.buildUserUrl("/phone/" + encodedPhone + "/user_dto");
            return apiClient.get(url, UserDTO.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}