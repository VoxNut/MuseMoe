package com.javaweb.converter;

import com.javaweb.entity.MediaEntity;
import com.javaweb.entity.RoleEntity;
import com.javaweb.entity.UserEntity;
import com.javaweb.enums.AccountStatus;
import com.javaweb.enums.MediaType;
import com.javaweb.enums.RoleType;
import com.javaweb.model.dto.UserDTO;
import com.javaweb.model.request.UserRequestDTO;
import com.javaweb.repository.RoleRepository;
import com.javaweb.utils.FileUtil;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserConverter implements EntityConverter<UserEntity, UserRequestDTO, UserDTO> {

    private final ModelMapper modelMapper;
    private final RoleRepository roleRepository;

    public UserDTO toDTO(UserEntity entity) {
        UserDTO result = modelMapper.map(entity, UserDTO.class);
        Set<String> roleDTOS = entity.getRoles() != null ? entity.getRoles().stream()
                .map(role -> "ROLE_" + role.getCode())
                .collect(Collectors.toCollection(LinkedHashSet::new)) : null;
        result.setRoles(roleDTOS);
        return result;
    }


    public UserEntity toEntity(UserRequestDTO userRequestDTO) {
        UserEntity result = modelMapper.map(userRequestDTO, UserEntity.class);
        MediaEntity media = new MediaEntity();
        media.setFileType(MediaType.IMAGE);
        if (userRequestDTO.getAvatar() == null) {
            result.setAvatar(null);
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
