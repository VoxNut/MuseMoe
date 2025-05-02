package com.javaweb.converter;

import com.javaweb.entity.AlbumEntity;
import com.javaweb.entity.ArtistEntity;
import com.javaweb.entity.MediaEntity;
import com.javaweb.enums.MediaType;
import com.javaweb.model.dto.AlbumDTO;
import com.javaweb.model.request.AlbumRequestDTO;
import com.javaweb.repository.ArtistRepository;
import com.javaweb.repository.MediaRepository;
import com.javaweb.utils.FileUtil;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
@RequiredArgsConstructor
public class AlbumConverter implements EntityConverter<AlbumEntity, AlbumRequestDTO, AlbumDTO> {
    private final ModelMapper modelMapper;
    private final ArtistRepository artistRepository;
    private final MediaRepository mediaRepository;

    @Override
    public AlbumDTO toDTO(AlbumEntity entity) {
        return null;
    }

    @Override
    public AlbumEntity toEntity(AlbumRequestDTO request) {
        AlbumEntity entity = modelMapper.map(request, AlbumEntity.class);

        if (request.getArtistId() != null) {
            ArtistEntity artist = artistRepository.findById(request.getArtistId()).orElse(null);
            entity.setArtist(artist);
        }
        if (request.getCoverArtPath() != null) {
            File file = new File(request.getCoverArtPath());
            MediaEntity mediaEntity = mediaRepository.findByFileUrl(request.getCoverArtPath())
                    .orElse(new MediaEntity(request.getCoverArtPath(), MediaType.IMAGE, FileUtil.getFileSize(file)));
            entity.setCoverArt(mediaEntity);
        }
        return entity;
    }
}
