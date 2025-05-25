package com.javaweb.service;

import com.javaweb.enums.AccountStatus;
import com.javaweb.enums.RoleType;
import com.javaweb.model.dto.PasswordDTO;
import com.javaweb.model.dto.UserDTO;
import com.javaweb.model.request.UserRequestDTO;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface UserService {

    UserDTO findOneByUsername(String userName);

    UserDTO findUserById(Long id);

    void updatePassword(long id, PasswordDTO userDTO);


    UserDTO updateProfileOfUser(String id, UserDTO userDTO);

    void delete(Set<Long> ids);


    boolean resetPassword(Long userId, String newPassword);


    void updateUser(UserRequestDTO userDTO);

    Boolean saveSignUpUser(UserRequestDTO userRequestDTO);

    Set<UserDTO> findAllUser(@RequestParam Map<String, Object> params, Set<String> roles);

    void saveUser(UserRequestDTO userRequestDTO);

    UserDTO findUserByUsername(String username);

    UserDTO findUserByEmail(String email);

    void updateLastLoginTime(LocalDateTime lastLoginTime);

    boolean upgradeUser(UserRequestDTO userRequestDTO);

    boolean checkUserArtist(Long currentArtistId);

    List<UserDTO> findAll(AccountStatus accountStatus);

    List<UserDTO> findFilteredUsers(LocalDateTime from, LocalDateTime to, RoleType roleType);
}
