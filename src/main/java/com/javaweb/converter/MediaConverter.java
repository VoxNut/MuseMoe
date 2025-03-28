package com.javaweb.converter;

import com.javaweb.entity.MediaEntity;
import com.javaweb.model.dto.MediaDTO;
import com.javaweb.model.request.MediaRequestDTO;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class MediaConverter implements EntityConverter<MediaEntity, MediaRequestDTO, MediaDTO> {


    private final ModelMapper modelMapper;

    @Override
    public MediaDTO toDTO(MediaEntity entity) {
        return modelMapper.map(entity, MediaDTO.class);
    }

    @Override
    public MediaEntity toEntity(MediaRequestDTO request) {
        return null;
    }
}
