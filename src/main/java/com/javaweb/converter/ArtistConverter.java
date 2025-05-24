package com.javaweb.converter;

import com.google.api.services.drive.model.File;
import com.javaweb.entity.ArtistEntity;
import com.javaweb.entity.StreamingMediaEntity;
import com.javaweb.model.dto.ArtistDTO;
import com.javaweb.model.request.ArtistRequestDTO;
import com.javaweb.service.StreamingMediaService;
import com.javaweb.service.impl.GoogleDriveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ArtistConverter implements EntityConverter<ArtistEntity, ArtistRequestDTO, ArtistDTO> {
    private final ModelMapper modelMapper;
    private final GoogleDriveService googleDriveService;
    private final StreamingMediaService streamingMediaService;

    @Override
    public ArtistDTO toDTO(ArtistEntity entity) {
        if (entity == null) {
            return null;
        }

        ArtistDTO artistDTO = modelMapper.map(entity, ArtistDTO.class);

        if (entity.getProfilePic() != null) {
            artistDTO.setProfilePictureId(entity.getProfilePic().getGoogleDriveId());
        }

        if (entity.getFollowers() != null) {
            artistDTO.setFollowerCount(entity.getFollowers().size());
        }
        if (entity.getAlbums() != null) {
            artistDTO.setAlbumCount(entity.getAlbums().size());
        }
        if (entity.getSongs() != null) {
            artistDTO.setSongCount(entity.getSongs().size());
        }

        return artistDTO;
    }

    @Override
    public ArtistEntity toEntity(ArtistRequestDTO request) {
        ArtistEntity entity = modelMapper.map(request, ArtistEntity.class);

        // Handle profile picture
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

                entity.setProfilePic(mediaEntity);
            } catch (Exception e) {
                log.error("Failed to process artist profile picture from Google Drive", e);
            }
        }
        return entity;
    }
}
