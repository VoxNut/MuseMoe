package com.javaweb.converter;

import com.javaweb.entity.SongLikesEntity;
import com.javaweb.model.dto.SongLikesDTO;
import com.javaweb.model.request.SongLikesRequestDTO;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SongLikesConverter implements EntityConverter<SongLikesEntity, SongLikesRequestDTO, SongLikesDTO> {

    private final UserConverter userConverter;
    private final SongConverter songConverter;
    private final ModelMapper modelMapper;

    @Override
    public SongLikesDTO toDTO(SongLikesEntity entity) {
        if (entity == null) {
            return null;
        }
        SongLikesDTO dto = new SongLikesDTO();

        dto.setId(entity.getId().getSongId());
        dto.setUserDTO(userConverter.toDTO(entity.getUser()));
        dto.setSongDTO(songConverter.toDTO(entity.getSong()));

        return dto;
    }

    @Override
    public SongLikesEntity toEntity(SongLikesRequestDTO request) {
        return null;
    }
}
