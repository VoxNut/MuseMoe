package com.javaweb.converter;

import com.javaweb.constant.AppConstant;
import com.javaweb.entity.Media;
import com.javaweb.entity.Role;
import com.javaweb.entity.User;
import com.javaweb.enums.AccountStatus;
import com.javaweb.enums.MediaType;
import com.javaweb.enums.RoleType;
import com.javaweb.model.dto.RoleDTO;
import com.javaweb.model.dto.UserDTO;
import com.javaweb.model.request.UserRequestDTO;
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
public class UserConverter implements EntityConverter<User,UserRequestDTO, UserDTO> {

    private final ModelMapper modelMapper;
    private final RoleConverter roleConverter;
    private final RoleRepository roleRepository;

    public UserDTO toDTO(User entity) {
        UserDTO result = modelMapper.map(entity, UserDTO.class);
        Set<RoleDTO> roleDTOS = entity.getRoles() != null ? entity.getRoles().stream().map(roleConverter::toDTO).collect(Collectors.toCollection(HashSet::new)) : new HashSet<>();
        result.setRoles(roleDTOS);
        String roleNames = entity.getRoles() != null ? entity.getRoles().stream().map(Role::getName).collect(Collectors.joining(",")) : "";
        result.setVisibleRoles(roleNames);
        return result;
    }



    public User toEntity(UserRequestDTO userRequestDTO) {
        User result = modelMapper.map(userRequestDTO, User.class);
        Media media = new Media();
        media.setFileType(MediaType.IMAGE);
        if (userRequestDTO.getAvatar() == null) {
            media.setFileSize(FileUtil.getFileSize(AppConstant.DEFAULT_USER_AVT_PATH));
            media.setFileUrl(AppConstant.DEFAULT_USER_AVT_PATH);
            result.setAvatar(media);
        }
        if (userRequestDTO.getRequestRoles() != null && !userRequestDTO.getRequestRoles().isEmpty()) {
            Set<Role> roleEntities = userRequestDTO.getRequestRoles()
                    .stream()
                    .map(roleRepository::findOneByName)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(HashSet::new));
            result.setRoles(roleEntities);
            media.setFileSize(FileUtil.getFileSize(userRequestDTO.getAvatar()));
            media.setFileUrl(userRequestDTO.getAvatar());
            result.setAvatar(media);
        }

        result.setAccountStatus(AccountStatus.ACTIVE);
        result.setRoles(Collections.singleton(roleRepository.findOneByCode(RoleType.FREE)));
        return result;
    }




}
