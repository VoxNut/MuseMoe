package com.javaweb.converter;

import com.google.api.services.drive.model.File;
import com.javaweb.entity.AlbumEntity;
import com.javaweb.entity.ArtistEntity;
import com.javaweb.entity.StreamingMediaEntity;
import com.javaweb.model.dto.AlbumDTO;
import com.javaweb.model.request.AlbumRequestDTO;
import com.javaweb.repository.ArtistRepository;
import com.javaweb.service.StreamingMediaService;
import com.javaweb.service.impl.GoogleDriveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AlbumConverter implements EntityConverter<AlbumEntity, AlbumRequestDTO, AlbumDTO> {
    private final ModelMapper modelMapper;
    private final ArtistRepository artistRepository;
    private final StreamingMediaService streamingMediaService;
    private final GoogleDriveService googleDriveService;

    @Override
    public AlbumDTO toDTO(AlbumEntity entity) {
        return modelMapper.map(entity, AlbumDTO.class);
    }

    @Override
    public AlbumEntity toEntity(AlbumRequestDTO request) {
        AlbumEntity entity = modelMapper.map(request, AlbumEntity.class);


        if (request.getGoogleDriveFileId() != null) {
            try {
                File driveFile = googleDriveService.getFileMetadata(request.getGoogleDriveFileId());

                StreamingMediaEntity mediaEntity = streamingMediaService.getOrCreateStreamingMedia(
                        driveFile.getId(),
                        driveFile.getName(),
                        driveFile.getMimeType(),
                        driveFile.getSize(),
                        driveFile.getWebContentLink()
                );

                entity.setCoverArt(mediaEntity);

            } catch (Exception e) {
                log.error("Failed to process album cover picture from Google Drive", e);
            }
        }


        if (request.getArtistId() != null) {
            ArtistEntity artist = artistRepository.findById(request.getArtistId()).orElse(null);
            entity.setArtist(artist);
        }

        return entity;
    }
}
