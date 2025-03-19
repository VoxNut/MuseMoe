package com.javaweb.converter;

import com.javaweb.entity.Role;
import com.javaweb.model.dto.RoleDTO;
import com.javaweb.model.request.RoleRequestDTO;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoleConverter implements EntityConverter<Role, RoleRequestDTO, RoleDTO> {

    private final ModelMapper modelMapper;

    public RoleDTO toDTO(Role roleEntity) {
        return modelMapper.map(roleEntity, RoleDTO.class);
    }

    public Role toEntity(RoleRequestDTO roleRequestDTO) {
        return modelMapper.map(roleRequestDTO, Role.class);
    }
}
