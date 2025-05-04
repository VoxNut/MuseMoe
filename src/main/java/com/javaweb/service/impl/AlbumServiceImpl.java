package com.javaweb.service.impl;

import com.javaweb.converter.AlbumConverter;
import com.javaweb.model.dto.AlbumDTO;
import com.javaweb.model.request.AlbumRequestDTO;
import com.javaweb.repository.AlbumRepository;
import com.javaweb.service.AlbumService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlbumServiceImpl implements AlbumService {

    private final AlbumRepository albumRepository;
    private final AlbumConverter albumConverter;
    private final GoogleDriveService googleDriveService;

    @Override
    public AlbumDTO createAlbum(AlbumRequestDTO albumRequestDTO) {
        try {
            if (albumRequestDTO.getAlbumCover() != null && !albumRequestDTO.getAlbumCover().isEmpty()) {
                String driveFileId = googleDriveService.uploadImageFile(
                        albumRequestDTO.getAlbumCover(),
                        GoogleDriveService.ALBUM_COVER_FOLDER_ID
                );
                albumRequestDTO.setGoogleDriveFileId(driveFileId);
                log.info("Uploaded album cover image to Google Drive with ID: {}", driveFileId);
            }
            AlbumDTO res = albumConverter.toDTO(albumRepository.save(albumConverter.toEntity(albumRequestDTO)));
            return res;
        } catch (Exception e) {
            log.error("Cannot create album: {}", e.getMessage(), e);
            return null;
        }
    }
}
