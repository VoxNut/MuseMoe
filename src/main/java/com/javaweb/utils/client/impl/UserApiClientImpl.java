package com.javaweb.utils.client.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.javaweb.enums.RoleType;
import com.javaweb.model.dto.UserDTO;
import com.javaweb.utils.client.ApiConfig;
import com.javaweb.utils.client.client_service.UserApiClient;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
class UserApiClientImpl implements UserApiClient {
    private final ApiClient apiClient;
    private final UrlEncoder urlEncoder;
    private final ResponseParser responseParser;
    private final ApiConfig apiConfig;


    @Override
    public Boolean updateUserPassword(Long id, String password) {
        try {
            String url = apiConfig.buildUserUrl("/reset_password");
            Map<String, Object> params = new HashMap<>();
            params.put("id", id);
            params.put("password", password);
            String responseEntity = apiClient.putWithFormParams(url, params);
            return responseParser.parseObject(responseEntity, Boolean.class);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Boolean createNewUser(String username, String password, String email) {
        try {

            String url = apiConfig.buildUserUrl("/register");
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode json = objectMapper.createObjectNode();
            json.put("username", username);
            json.put("email", email);
            json.put("password", password);
            String responseEntity = apiClient.post(url, json.toString());
            return responseParser.parseObject(responseEntity, Boolean.class);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public UserDTO fetchUserByEmail(String email) {
        try {
            String url = apiConfig.buildUserUrl("/email?email=" + email);
            String responseEntity = apiClient.get(url);
            return responseParser.parseObject(responseEntity, UserDTO.class);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Set<UserDTO> fetchAllUsersBaseOnRole(RoleType role) {
        try {
            String url = apiConfig.buildUserUrl("?status=1&roles=" + role);
            String responseBody = apiClient.get(url);

            Set<UserDTO> users = responseParser.parseReference(
                    responseBody,
                    new TypeReference<Set<UserDTO>>() {
                    }
            );

            return users.stream()
                    .filter(UserDTO::hasFullName)
                    .collect(Collectors.toCollection(TreeSet::new));
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptySet();
        }
    }

    @Override
    public void updateLastLoginTime() {
        try {
            String url = apiConfig.buildUserUrl("/last_login");
            apiClient.putSimple(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public UserDTO fetchUserByUsername(String username) {
        try {
            String encodedUsername = urlEncoder.encode(username);
            String url = apiConfig.buildUserUrl("/username/" + encodedUsername + "/user_dto");
            String responseBody = apiClient.get(url);

            return responseParser.parseObject(responseBody, UserDTO.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public UserDTO fetchUserById(Long id) {
        try {
            String url = apiConfig.buildUserUrl("/" + id + "/user_dto");
            String responseBody = apiClient.get(url);

            return responseParser.parseObject(responseBody, UserDTO.class);
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
            String responseBody = apiClient.get(url);

            return responseParser.parseObject(responseBody, UserDTO.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}