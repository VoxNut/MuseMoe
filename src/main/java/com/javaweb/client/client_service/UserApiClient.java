package com.javaweb.client.client_service;

import com.javaweb.enums.RoleType;
import com.javaweb.model.dto.UserDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
import java.util.Set;

public interface UserApiClient {

    Set<UserDTO> fetchAllUsersBaseOnRole(RoleType role);


    UserDTO fetchUserByUsername(String username);

    UserDTO fetchUserById(Long id);


    UserDTO fetchUserByPhone(String phone);

    Boolean updateUserPassword(Long id, String password);

    Boolean createNewUser(String username, String fullName, String password, String email);

    UserDTO fetchUserByEmail(String email);

    void updateLastLoginTime();

    UserDTO fetchCurrentUser();

    boolean upgradeUser(RoleType roleType);

    boolean checkUserArtist(Long currentArtistId);

    List<UserDTO> fetchAllUsers();

    List<UserDTO> fetchUsersByFilter(Date from, Date to, RoleType roleType);

    boolean updateUserProfile(String fullName, String email, MultipartFile profilePicture);

    boolean changeUserPassword(String newPassword);

    boolean checkCurrentPassword(String currentPassword);

    boolean closeUserAccount();
}
