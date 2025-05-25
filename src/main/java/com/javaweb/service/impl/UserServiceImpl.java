package com.javaweb.service.impl;

import com.google.api.services.drive.model.File;
import com.javaweb.converter.UserConverter;
import com.javaweb.entity.RoleEntity;
import com.javaweb.entity.StreamingMediaEntity;
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
import com.javaweb.service.StreamingMediaService;
import com.javaweb.service.UserService;
import com.javaweb.utils.SecurityUtils;
import com.javaweb.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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
    private final StreamingMediaService streamingMediaService;

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
        try {
            return userConverter.toDTO(userRepository.findByEmail(email)
                    .orElseThrow(() -> new EntityNotFoundException("User with email: " + email + " not found!")));
        } catch (Exception e) {
            return null;
        }
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
    public boolean updateUser(UserRequestDTO userRequestDTO) {
        Long userId = Objects.requireNonNull(SecurityUtils.getPrincipal()).getId();
        try {
            UserEntity userEntity = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("UserEntity not found!"));

            boolean updated = false;

            if (StringUtils.isNotBlank(userRequestDTO.getFullName()) &&
                    !userRequestDTO.getFullName().equals(userEntity.getFullName())) {
                userEntity.setFullName(userRequestDTO.getFullName());
                updated = true;
            }

            if (StringUtils.isNotBlank(userRequestDTO.getEmail()) &&
                    !userRequestDTO.getEmail().equals(userEntity.getEmail())) {
                userEntity.setEmail(userRequestDTO.getEmail());
                updated = true;
            }

            if (userRequestDTO.getUserAvatar() != null && !userRequestDTO.getUserAvatar().isEmpty()) {
                String currentAvatarId = userEntity.getAvatar().getGoogleDriveId();
                boolean shouldUpdateAvatar = true;

                if (StringUtils.isNotBlank(currentAvatarId)) {
                    try {
                        long newFileSize = userRequestDTO.getUserAvatar().getSize();
                        String newFileName = userRequestDTO.getUserAvatar().getOriginalFilename();

                        File existingFile = googleDriveService.getFileMetadata(currentAvatarId);

                        if (existingFile != null &&
                                existingFile.getSize() != null &&
                                existingFile.getSize() == newFileSize &&
                                existingFile.getName().equals(newFileName)) {
                            shouldUpdateAvatar = false;
                            log.info("Avatar file appears unchanged, skipping upload");
                        }
                    } catch (Exception e) {
                        log.warn("Could not compare avatar files, will upload new one", e);
                    }
                }

                if (shouldUpdateAvatar) {
                    String newDriveFileId = googleDriveService.uploadImageFile(
                            userRequestDTO.getUserAvatar(),
                            GoogleDriveService.AVATAR_FOLDER_ID
                    );


                    if (StringUtils.isNotBlank(currentAvatarId)) {
                        if (googleDriveService.deleteFile(currentAvatarId)) {
                            log.info("Deleted old avatar file with ID: {}", currentAvatarId);
                        } else {
                            log.warn("Failed to delete old avatar file with ID: {}", currentAvatarId);
                            throw new Exception("Failed to delete old avatar file with ID: " + currentAvatarId);
                        }
                    }

                    File driveFile = googleDriveService.getFileMetadata(newDriveFileId);
                    StreamingMediaEntity mediaEntity = streamingMediaService.getOrCreateStreamingMedia(
                            driveFile.getId(),
                            driveFile.getName(),
                            driveFile.getMimeType(),
                            driveFile.getSize(),
                            driveFile.getWebContentLink()
                    );

                    userEntity.setAvatar(mediaEntity);
                    updated = true;
                    log.info("Updated user avatar with new Google Drive file ID: {}", newDriveFileId);
                }
            }

            if (updated) {
                userRepository.save(userEntity);
            } else {
                log.info("No changes detected for user ID: {}, skipping update", userId);
            }
            return true;
        } catch (Exception e) {
            log.error("Cannot update user with id: {}", userId, e);
            return false;
        }
    }


    @Override
    public void saveUser(UserRequestDTO userRequestDTO) {
        UserEntity UserEntity = userConverter.toEntity(userRequestDTO);
        UserEntity.setPassword(passwordService.encodePassword(userRequestDTO.getPassword()));
        userRepository.save(UserEntity);
    }

    @Override
    public UserDTO findUserByUsername(String username) {
        try {
            return userConverter.toDTO(userRepository.findOneByUsername(username));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Set<UserDTO> findAllUser(Map<String, Object> params, Set<String> roles) {
        return null;
    }

    @Override
    public boolean checkUserArtist(Long currentArtistId) {
        Long userId = SecurityUtils.getPrincipal().getId();
        try {
            UserEntity curUser = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User with id: " + userId + " not found!"));
            if (curUser.getArtist() == null) {
                return false;
            }
            return curUser.getArtist().getId().equals(currentArtistId);
        } catch (Exception e) {
            log.error("Cannot check user with id: {} artist role", userId);
            return false;
        }
    }

    @Override
    public List<UserDTO> findAll(AccountStatus accountStatus) {
        List<UserDTO> userDTOS = userRepository.findByAccountStatus(accountStatus).stream().map(userConverter::toDTO).toList();
        return userDTOS;
    }

    @Override
    public List<UserDTO> findFilteredUsers(LocalDateTime from, LocalDateTime to, RoleType roleType) {
        try {
            List<UserDTO> res = userRepository
                    .findFilteredUsers(from, to, roleType)
                    .stream()
                    .map(userConverter::toDTO)
                    .collect(Collectors.toList());
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }

    }

    @Override
    public boolean changePassword(String newPassword) {
        try {
            Long userId = Objects.requireNonNull(SecurityUtils.getPrincipal()).getId();

            UserEntity userEntity = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));


            String encodedPassword = passwordService.encodePassword(newPassword);
            userEntity.setPassword(encodedPassword);

            userRepository.save(userEntity);
            log.info("Password successfully changed for user ID: {}", userId);
            return true;
        } catch (Exception e) {
            log.error("Error changing password", e);
            return false;
        }
    }

    @Override
    public boolean checkCurrentPassword(String currentPassword) {
        try {
            Long userId = Objects.requireNonNull(SecurityUtils.getPrincipal()).getId();
            UserEntity userEntity = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));
            return passwordService.matches(currentPassword, userEntity.getPassword());
        } catch (Exception e) {
            log.error("Error checking current password", e);
            return false;
        }
    }

    @Override
    public boolean closeAccount(AccountStatus accountStatus) {
        try {
            Long userId = Objects.requireNonNull(SecurityUtils.getPrincipal()).getId();

            UserEntity userEntity = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));

            userEntity.setAccountStatus(AccountStatus.DELETED);

            userEntity.setUpdated_at(LocalDateTime.now());

            userRepository.save(userEntity);


            log.info("Account successfully closed for user ID: {}", userId);
            return true;
        } catch (Exception e) {
            log.error("Error closing user account", e);
            return false;
        }
    }

}
