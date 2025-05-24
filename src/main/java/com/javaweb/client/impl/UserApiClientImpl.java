package com.javaweb.client.impl;

import com.javaweb.client.ApiConfig;
import com.javaweb.client.client_service.UserApiClient;
import com.javaweb.enums.RoleType;
import com.javaweb.model.dto.UserDTO;
import com.javaweb.model.request.UserRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.*;

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
    public boolean upgradeUser(RoleType roleType) {
        try {
            String url = apiConfig.buildUserUrl("/upgrade");
            return apiClient.put(url,
                    UserRequestDTO.builder()
                            .roleType(roleType)
                            .build()
                    , Boolean.class);
        } catch (Exception e) {
            log.error("Upgrade to premium user failed!", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public Boolean createNewUser(String username, String fullName, String password, String email) {
        try {
            String url = apiConfig.buildUserUrl("/register");

            // Create a map of parts for multipart form data
            Map<String, Object> parts = new HashMap<>();
            parts.put("username", username);
            parts.put("password", password);
            parts.put("email", email);
            parts.put("fullName", fullName);

            Object result = apiClient.postMultipart(url, parts, Object.class);
            return result != null;
        } catch (Exception e) {
            log.error("Error creating artist: {}", e.getMessage(), e);
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

    @Override
    public boolean checkUserArtist(Long currentArtistId) {
        try {
            String url = apiConfig.buildUserUrl("/check_user_artist?currentArtistId=" + currentArtistId);
            return apiClient.get(url, Boolean.class);
        } catch (Exception e) {
            log.error("Cannot check user artist role!", e.getMessage());
            return false;

        }
    }

    @Override
    public List<UserDTO> fetchAllUsers() {
        try {
            String url = apiConfig.buildUserUrl("/all?accountStatus=ACTIVE");
            List<UserDTO> users = apiClient.getList(url, UserDTO.class);
            return users;
        } catch (Exception e) {
            log.error("Error fetching all users!", e.getMessage());
            return Collections.emptyList();
        }
    }
}