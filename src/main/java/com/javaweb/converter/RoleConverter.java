package com.javaweb.converter;

import com.javaweb.entity.RoleEntity;
import com.javaweb.model.dto.RoleDTO;
import com.javaweb.model.request.RoleRequestDTO;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoleConverter implements EntityConverter<RoleEntity, RoleRequestDTO, RoleDTO> {

    private final ModelMapper modelMapper;

    public RoleDTO toDTO(RoleEntity roleEntity) {
        return modelMapper.map(roleEntity, RoleDTO.class);
    }

    public RoleEntity toEntity(RoleRequestDTO roleRequestDTO) {
        return modelMapper.map(roleRequestDTO, RoleEntity.class);
    }
}
