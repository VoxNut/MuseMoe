package com.javaweb.converter;

import com.google.api.services.drive.model.File;
import com.javaweb.entity.AlbumEntity;
import com.javaweb.entity.ArtistEntity;
import com.javaweb.entity.StreamingMediaEntity;
import com.javaweb.model.dto.AlbumDTO;
import com.javaweb.model.request.AlbumRequestDTO;
import com.javaweb.repository.ArtistRepository;
import com.javaweb.repository.StreamingMediaRepository;
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
    private final StreamingMediaRepository streamingMediaRepository;

    @Override
    public AlbumDTO toDTO(AlbumEntity entity) {

        AlbumDTO dto = modelMapper.map(entity, AlbumDTO.class);

        if (entity.getCoverArt() != null && entity.getCoverArt().getGoogleDriveId() != null) {
            try {
                dto.setImageId(entity.getCoverArt().getGoogleDriveId());
            } catch (Exception e) {
                log.error("Failed to process album cover picture from Google Drive", e);
            }
        }
        if (entity.getArtist() != null) {
            dto.setArtistName(entity.getArtist().getStageName());
        }
        return dto;
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
        } else {
            StreamingMediaEntity mediaEntity = streamingMediaRepository.findById(1039L).orElse(null);
            entity.setCoverArt(mediaEntity);
        }


        if (request.getArtistId() != null) {
            ArtistEntity artist = artistRepository.findById(request.getArtistId()).orElse(null);
            entity.setArtist(artist);
        }

        return entity;
    }
}
