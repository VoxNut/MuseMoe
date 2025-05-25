package com.javaweb.converter;

import com.javaweb.entity.PlaylistEntity;
import com.javaweb.entity.PlaylistSongEntity;
import com.javaweb.entity.SongEntity;
import com.javaweb.model.dto.PlaylistDTO;
import com.javaweb.model.dto.SongDTO;
import com.javaweb.model.request.PlaylistRequestDTO;
import com.javaweb.repository.UserRepository;
import com.javaweb.utils.DateUtil;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
public class PlaylistConverter implements EntityConverter<PlaylistEntity, PlaylistRequestDTO, PlaylistDTO> {

    private final ModelMapper modelMapper;
    private final SongConverter songConverter;
    private final UserRepository userRepository;

    @Override
    public PlaylistDTO toDTO(PlaylistEntity entity) {
        PlaylistDTO res = modelMapper.map(entity, PlaylistDTO.class);

        if (entity.getUser() != null) {
            res.setCreatedBy(entity.getUser().getUsername());
        }


        if (entity.getPlaylistSongEntities() != null) {
            res.setSongs(
                    entity
                            .getPlaylistSongEntities()
                            .stream()
                            .map(playlistSongEntity -> {
                                SongDTO songDTO = songConverter.toDTO(playlistSongEntity.getSong());
                                songDTO.setPosition(playlistSongEntity.getPosition());
                                return songDTO;
                            })
                            .sorted(Comparator.comparing(SongDTO::getPosition))
                            .collect(Collectors.toList())
            );

            res.setSongIds(
                    entity.getPlaylistSongEntities()
                            .stream()
                            .map(playlistSongEntity -> playlistSongEntity.getSong().getId())
                            .collect(Collectors.toList())
            );


            res.setTotalDuration(
                    entity
                            .getPlaylistSongEntities()
                            .stream()
                            .map(PlaylistSongEntity::getSong)
                            .mapToInt(SongEntity::getDuration)
                            .sum()
            );
        }

        res.setCreatedDate(DateUtil.toDate(entity.getCreated_at()));
        res.setUpdateDate(DateUtil.toDate(entity.getUpdated_at()));


        return res;
    }

    @Override
    public PlaylistEntity toEntity(PlaylistRequestDTO request) {
        return null;
    }
}
