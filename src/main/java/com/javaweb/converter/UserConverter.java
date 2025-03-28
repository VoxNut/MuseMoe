package com.javaweb.converter;

import com.javaweb.constant.AppConstant;
import com.javaweb.entity.MediaEntity;
import com.javaweb.entity.RoleEntity;
import com.javaweb.entity.UserEntity;
import com.javaweb.enums.AccountStatus;
import com.javaweb.enums.MediaType;
import com.javaweb.enums.RoleType;
import com.javaweb.exception.EntityNotFoundException;
import com.javaweb.model.dto.RoleDTO;
import com.javaweb.model.dto.UserDTO;
import com.javaweb.model.request.UserRequestDTO;
import com.javaweb.repository.MediaRepository;
import com.javaweb.repository.RoleRepository;
import com.javaweb.utils.FileUtil;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserConverter implements EntityConverter<UserEntity, UserRequestDTO, UserDTO> {

    private final ModelMapper modelMapper;
    private final RoleConverter roleConverter;
    private final RoleRepository roleRepository;
    private final MediaRepository mediaRepository;

    public UserDTO toDTO(UserEntity entity) {
        UserDTO result = modelMapper.map(entity, UserDTO.class);
        Set<RoleDTO> roleDTOS = entity.getRoles() != null ? entity.getRoles().stream().map(roleConverter::toDTO).collect(Collectors.toCollection(HashSet::new)) : new HashSet<>();
        result.setRoles(roleDTOS);
        String roleNames = entity.getRoles() != null ? entity.getRoles().stream().map(RoleEntity::getName).collect(Collectors.joining(",")) : "";
        result.setVisibleRoles(roleNames);
        return result;
    }


    public UserEntity toEntity(UserRequestDTO userRequestDTO) {
        UserEntity result = modelMapper.map(userRequestDTO, UserEntity.class);
        MediaEntity media = new MediaEntity();
        media.setFileType(MediaType.IMAGE);
        if (userRequestDTO.getAvatar() == null) {
            media = mediaRepository.findByFileUrl(AppConstant.DEFAULT_USER_AVT_PATH)
                    .orElseThrow(() -> new EntityNotFoundException("Default avatar not found!"));
            result.setAvatar(media);
        }

        if (userRequestDTO.getRequestRoles() != null && !userRequestDTO.getRequestRoles().isEmpty()) {
            Set<RoleEntity> roleEntities = userRequestDTO.getRequestRoles()
                    .stream()
                    .map(roleRepository::findOneByName)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(HashSet::new));
            result.setRoles(roleEntities);
            media.setFileSize(FileUtil.getFileSize(userRequestDTO.getAvatar()));
            media.setFileUrl(userRequestDTO.getAvatar());
            result.setAvatar(media);
        } else {
            RoleEntity roleEntity = roleRepository.findOneByCode(RoleType.FREE);
            result.setRoles(Collections.singleton(roleEntity));
        }
        result.setPreferredLanguage("en");
        result.setAccountStatus(AccountStatus.ACTIVE);
        return result;
    }


}
