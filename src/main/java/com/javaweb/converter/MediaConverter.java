package com.javaweb.converter;

import com.javaweb.entity.Media;
import com.javaweb.model.dto.MediaDTO;
import com.javaweb.model.request.MediaRequestDTO;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;

@RequiredArgsConstructor
public class MediaConverter implements EntityConverter<Media, MediaRequestDTO, MediaDTO>{


    private final ModelMapper modelMapper;

    @Override
    public MediaDTO toDTO(Media entity) {
        return modelMapper.map(entity, MediaDTO.class);
    }

    @Override
    public Media toEntity(MediaRequestDTO request) {
        return null;
    }
}
