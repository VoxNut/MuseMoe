package com.javaweb.converter;

import com.google.api.services.drive.model.File;
import com.javaweb.entity.RoleEntity;
import com.javaweb.entity.StreamingMediaEntity;
import com.javaweb.entity.UserEntity;
import com.javaweb.enums.AccountStatus;
import com.javaweb.enums.RoleType;
import com.javaweb.model.dto.UserDTO;
import com.javaweb.model.request.UserRequestDTO;
import com.javaweb.repository.RoleRepository;
import com.javaweb.repository.StreamingMediaRepository;
import com.javaweb.service.StreamingMediaService;
import com.javaweb.service.impl.GoogleDriveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserConverter implements EntityConverter<UserEntity, UserRequestDTO, UserDTO> {

    private final ModelMapper modelMapper;
    private final RoleRepository roleRepository;
    private final StreamingMediaRepository mediaRepository;
    private final GoogleDriveService googleDriveService;
    private final StreamingMediaService streamingMediaService;

    public UserDTO toDTO(UserEntity entity) {
        if (entity == null) {
            return null;
        }
        UserDTO result = modelMapper.map(entity, UserDTO.class);
        Set<String> roleDTOS = entity.getRoles() != null ? entity.getRoles().stream()
                .map(role -> "ROLE_" + role.getCode())
                .collect(Collectors.toCollection(LinkedHashSet::new)) : null;
        result.setRoles(roleDTOS);
        return result;
    }


    public UserEntity toEntity(UserRequestDTO request) {
        UserEntity entity = modelMapper.map(request, UserEntity.class);


        if (request.getGoogleDriveFileId() != null) {
            try {
                File driveFile = googleDriveService.getFileMetadata(request.getGoogleDriveFileId());

                StreamingMediaEntity mediaEntity = streamingMediaService.getOrCreateStreamingMedia(
                        driveFile.getId(),
                        driveFile.getName(),
                        driveFile.getMimeType(),
                        driveFile.getSize(),
                        driveFile.getWebContentLink()
                );

                entity.setAvatar(mediaEntity);

            } catch (Exception e) {
                log.error("Failed to process album cover picture from Google Drive", e);
            }
        } else {
            //default profile
            StreamingMediaEntity mediaEntity = mediaRepository.findById(1038L).orElse(null);
            entity.setAvatar(mediaEntity);
        }

        if (request.getRequestRoles() != null && !request.getRequestRoles().isEmpty()) {
            Set<RoleEntity> roleEntities = request.getRequestRoles()
                    .stream()
                    .map(roleRepository::findOneByName)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(HashSet::new));
            entity.setRoles(roleEntities);
        } else {
            RoleEntity roleEntity = roleRepository.findOneByCode(RoleType.FREE);
            entity.setRoles(Collections.singleton(roleEntity));
        }
        entity.setPreferredLanguage("en");
        entity.setAccountStatus(AccountStatus.ACTIVE);
        return entity;
    }


}
