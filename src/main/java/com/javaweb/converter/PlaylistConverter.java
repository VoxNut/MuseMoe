package com.javaweb.converter;

import com.javaweb.entity.PlaylistEntity;
import com.javaweb.entity.PlaylistSongEntity;
import com.javaweb.model.dto.PlaylistDTO;
import com.javaweb.model.request.PlaylistRequestDTO;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
public class PlaylistConverter implements EntityConverter<PlaylistEntity, PlaylistRequestDTO, PlaylistDTO> {

    private final ModelMapper modelMapper;
    private final SongConverter songConverter;

    @Override
    public PlaylistDTO toDTO(PlaylistEntity entity) {
        PlaylistDTO res = modelMapper.map(entity, PlaylistDTO.class);
        res.setSongs(
                entity.getPlaylistSongEntities()
                        .stream()
                        .map(PlaylistSongEntity::getSong)
                        .map(songConverter::toDTO)
                        .collect(Collectors.toList())
        );
        return res;
    }

    @Override
    public PlaylistEntity toEntity(PlaylistRequestDTO request) {
        return null;
    }
}
