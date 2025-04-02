package com.javaweb.converter;

import com.javaweb.entity.SongEntity;
import com.javaweb.model.dto.SongDTO;
import com.javaweb.model.request.SongRequestDTO;
import com.javaweb.utils.Mp3Util;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SongConverter implements EntityConverter<SongEntity, SongRequestDTO, SongDTO> {

    private final ModelMapper modelMapper;


    private final Mp3Util mp3Util;

    @Override
    public SongDTO toDTO(SongEntity entity) {
        if (entity == null) {
            return null;
        }
        SongDTO dto = modelMapper.map(entity, SongDTO.class);

        dto.setSongTitle(entity.getTitle());
        dto.setAudioFilePath(entity.getAudioFile().getFileUrl());
        if (entity.getAlbum() != null) {
            dto.setAlbum(entity.getAlbum().getTitle());
        }
        if (entity.getArtists() != null) {
            dto.setSongArtist(entity.getArtists().stream()
                    .map(artist -> artist.getUser().getFullName())
                    .collect(Collectors.joining(", ")));
        }

        return dto;
    }

    @Override
    public SongEntity toEntity(SongRequestDTO request) {

        return null;
    }
}
