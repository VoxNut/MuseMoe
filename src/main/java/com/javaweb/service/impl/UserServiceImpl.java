package com.javaweb.service.impl;

import com.javaweb.converter.UserConverter;
import com.javaweb.entity.RoleEntity;
import com.javaweb.entity.UserEntity;
import com.javaweb.enums.AccountStatus;
import com.javaweb.enums.RoleType;
import com.javaweb.exception.EntityNotFoundException;
import com.javaweb.model.dto.PasswordDTO;
import com.javaweb.model.dto.UserDTO;
import com.javaweb.model.request.UserRequestDTO;
import com.javaweb.repository.RoleRepository;
import com.javaweb.repository.UserRepository;
import com.javaweb.service.PasswordService;
import com.javaweb.service.UserService;
import com.javaweb.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordService passwordService;
    private final UserConverter userConverter;
    private final GoogleDriveService googleDriveService;

    @Override
    public boolean upgradeUser(UserRequestDTO userRequestDTO) {
        try {
            RoleType roleType = userRequestDTO.getRoleType();
            RoleEntity roleEntity = roleRepository.findOneByCode(roleType);
            Long userId = Objects.requireNonNull(SecurityUtils.getPrincipal()).getId();
            UserEntity user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("UserEntity not found!"));
            if (!user.getRoles().contains(roleEntity)) {
                user.getRoles().add(roleEntity);
            }
            userRepository.save(user);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public UserDTO findOneByUsername(String userName) {
        UserEntity UserEntity = userRepository.findOneByUsername(userName);
        return UserEntity != null ? userConverter.toDTO(UserEntity) : null;
    }

    @Override
    public UserDTO findUserById(Long id) {
        UserEntity entity = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("UserEntity not found!"));
        return userConverter.toDTO(entity);
    }


    @Override
    public void updatePassword(long id, PasswordDTO passwordDTO) {
        UserEntity user = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("UserEntity not found!"));
        if (passwordService.matches(passwordDTO.getOldPassword(), passwordDTO.getNewPassword()) && passwordDTO.getNewPassword().equals(passwordDTO.getConfirmPassword())) {
            user.setPassword(passwordService.encodePassword(passwordDTO.getNewPassword()));
            userRepository.save(user);
        } else {
        }
    }


    @Override
    public UserDTO updateProfileOfUser(String username, UserDTO updateUser) {
        UserEntity oldUser = userRepository.findOneByUsername(username);
        oldUser.setFullName(updateUser.getFullName());
        return userConverter.toDTO(userRepository.save(oldUser));
    }

    @Override
    public void delete(Set<Long> ids) {
        for (Long item : ids) {
            UserEntity UserEntity = userRepository.findById(item).orElseThrow(() -> new EntityNotFoundException("UserEntity not found!"));
            UserEntity.setAccountStatus(AccountStatus.INACTIVE);
            userRepository.save(UserEntity);
        }
    }

    public boolean resetPassword(Long userId, String newPassword) {
        return userRepository.findById(userId).map(user -> {
            user.setPassword(passwordService.encodePassword(newPassword));
            userRepository.save(user);
            return true;
        }).orElse(false);
    }

    @Override
    public UserDTO findUserByEmail(String email) {
        return userConverter.toDTO(userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User with email: " + email + " not found!"))
        );
    }

    @Override
    public void updateLastLoginTime(LocalDateTime lastLogin) {
        Long currentUserId = Objects.requireNonNull(SecurityUtils.getPrincipal()).getId();
        UserEntity user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("User with not found!"));

        user.setLastLogin(lastLogin);
        userRepository.save(user);
    }

    @Override
    public Boolean saveSignUpUser(UserRequestDTO userRequestDTO) {
        try {
            if (userRequestDTO.getUserAvatar() != null && !userRequestDTO.getUserAvatar().isEmpty()) {
                String driveFileId = googleDriveService.uploadImageFile(
                        userRequestDTO.getUserAvatar(),
                        GoogleDriveService.AVATAR_FOLDER_ID
                );
                userRequestDTO.setGoogleDriveFileId(driveFileId);
                log.info("Uploaded artist profile image to Google Drive with ID: {}", driveFileId);
            }

            userRequestDTO.setPassword(passwordService.encodePassword(userRequestDTO.getPassword()));
            UserEntity UserEntity = userConverter.toEntity(userRequestDTO);
            userRepository.save(UserEntity);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    @Override
    public void updateUser(UserRequestDTO userRequestDTO) {
//        UserEntity existingUser = userRepository.findById(userRequestDTO.getId()).orElseThrow(() -> new EntityNotFoundException("UserEntity not found!"));
//        if (userRequestDTO.getPassword().isEmpty()) {
//            userRequestDTO.setPassword(existingUser.getPassword());
//        } else {
//            userRequestDTO.setPassword(passwordService.encodePassword(userRequestDTO.getPassword()));
//        }
//        UserEntity UserEntity = userConverter.convertToEntity(userRequestDTO);
//        UserEntity.setCustomer(existingUser.getCustomer());
//        if (UserEntity.getCustomer() != null) {
//            CustomerEntity customerEntity = UserEntity.getCustomer();
//            customerEntity.setFullName(userRequestDTO.getFullName());
//            customerEntity.setEmail(userRequestDTO.getEmail());
//            customerEntity.setPhone(userRequestDTO.getPhone());
//            customerRepository.save(customerEntity);
//        }
//        UserEntity.setOrders(existingUser.getOrders());
//
//        UserEntity.setProducts(existingUser.getProducts());
//
//        userRepository.save(UserEntity);
    }


    @Override
    public void saveUser(UserRequestDTO userRequestDTO) {
        UserEntity UserEntity = userConverter.toEntity(userRequestDTO);
        UserEntity.setPassword(passwordService.encodePassword(userRequestDTO.getPassword()));
        userRepository.save(UserEntity);
    }

    @Override
    public UserDTO findUserByUsername(String username) {
        return userConverter.toDTO(userRepository.findOneByUsername(username));
    }

    @Override
    public Set<UserDTO> findAllUser(Map<String, Object> params, Set<String> roles) {
        return null;
    }


}
