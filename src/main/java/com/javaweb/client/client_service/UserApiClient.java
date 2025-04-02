package com.javaweb.client.client_service;

import com.javaweb.enums.RoleType;
import com.javaweb.model.dto.UserDTO;

import java.util.Set;

public interface UserApiClient {

    Set<UserDTO> fetchAllUsersBaseOnRole(RoleType role);


    UserDTO fetchUserByUsername(String username);

    UserDTO fetchUserById(Long id);


    UserDTO fetchUserByPhone(String phone);

    Boolean updateUserPassword(Long id, String password);

    Boolean createNewUser(String username, String password, String email);

    UserDTO fetchUserByEmail(String email);

    void updateLastLoginTime();
}