package com.javaweb.service.impl;


import com.javaweb.converter.SongConverter;
import com.javaweb.entity.SongEntity;
import com.javaweb.exception.EntityNotFoundException;
import com.javaweb.model.dto.SongDTO;
import com.javaweb.model.request.SongRequestDTO;
import com.javaweb.repository.SongRepository;
import com.javaweb.service.SongService;
import com.javaweb.service.TagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class SongServiceImpl implements SongService {


    private final SongRepository songRepository;

    private final SongConverter songConverter;


    private final TagService tagService;

    private final GoogleDriveService googleDriveService;


    private final SharedSearchService sharedSearchService;

    @Override
    public SongDTO findOneByTitle(String title) {
        SongDTO song = songConverter.toDTO(
                songRepository.findOneByTitle(title)
                        .orElseThrow(() -> new EntityNotFoundException("Song not found!"))
        );
        return song;
    }

    @Override
    public List<SongDTO> findAllSongsLike(String title) {
        try {
            List<SongDTO> songDTOS = songRepository.findAllSongsLike(title)
                    .stream()
                    .map(songConverter::toDTO)
                    .collect(Collectors.toList());
            return songDTOS;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @Override
    public SongDTO findById(Long id) {
        return songConverter.toDTO(songRepository
                .findById(id)
                .orElseThrow(() -> {
                            log.warn("Song with id: {} not found", id);
                            return new RuntimeException("Failed to find song");
                        }
                ));
    }

    @Override
    public SongDTO findSongByUrl(String songUrl) {
        return songRepository.findByStreamingMediaWebContentLink(songUrl)
                .map(songConverter::toDTO)
                .orElseThrow(() -> new EntityNotFoundException("Song with URL " + songUrl + " not found"));
    }

    @Override
    public List<SongDTO> findAllSongs() {
        try {
            return songRepository.findAll()
                    .stream()
                    .map(songConverter::toDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @Override
    @Transactional
    public Map<String, Object> createMultipleSongs(SongRequestDTO songRequestDTO) {
        List<String> successfulUploads = new ArrayList<>();
        List<String> failedUploads = new ArrayList<>();

        if (songRequestDTO.getMp3Files() == null || songRequestDTO.getMp3Files().isEmpty()) {
            return Map.of(
                    "success", false,
                    "message", "No files provided for upload"
            );
        }

        try {
            List<Map<String, String>> uploadResults = googleDriveService.uploadMultipleSongFiles(songRequestDTO.getMp3Files());

            for (Map<String, String> uploadResult : uploadResults) {
                String fileName = uploadResult.get("fileName");
                String fileId = uploadResult.get("fileId");
                if (uploadResult.containsKey("error")) {
                    failedUploads.add(fileName + " (Error: " + uploadResult.get("error") + ")");
                    continue;
                }
                songRequestDTO.setGoogleDriveFileId(fileId);
                SongEntity song = songConverter.toEntity(songRequestDTO);
                song.setTags(tagService.generateTagsForSong(song));
                songRepository.save(song);
                successfulUploads.add(fileName);
            }
        } catch (Exception e) {
            log.error("Error in batch upload process: {}", e.getMessage(), e);
            return Map.of(
                    "success", false,
                    "message", "Error processing batch upload: " + e.getMessage()
            );
        }

        return Map.of(
                "success", !successfulUploads.isEmpty(),
                "totalFiles", songRequestDTO.getMp3Files().size(),
                "successful", successfulUploads,
                "failed", failedUploads,
                "successCount", successfulUploads.size(),
                "failureCount", failedUploads.size()
        );
    }

    @Override
    public SongDTO findByGoogleDriveId(String driveId) {
        try {
            SongEntity entity = songRepository.findByStreamingMediaGoogleDriveId(driveId);
            SongDTO res = songConverter.toDTO(entity);
            log.info("Successfully find song with {} drive Id", driveId);
            return res;
        } catch (Exception e) {
            log.error("Failed find song with {} drive Id", driveId);
            return null;
        }
    }

    @Override
    public boolean createSong(SongRequestDTO songRequestDTO) {
        try {

            songRequestDTO.setGoogleDriveFileId(
                    googleDriveService.uploadSongFile(songRequestDTO.getMp3Files().getFirst()));

            SongEntity song = songConverter.toEntity(songRequestDTO);
            song.setTags(tagService.generateTagsForSong(song));
            songRepository.save(song);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public int importSongsFromGoogleDrive() {
        List<GoogleDriveService.DriveFileBundle> songBundles = googleDriveService.loadAllSongsWithMetadata();

        int importedCount = 0;

        for (GoogleDriveService.DriveFileBundle bundle : songBundles) {
            try {
                // Create song request DTO
                SongRequestDTO songRequestDTO = new SongRequestDTO();

                // Set Google Drive file ID
                songRequestDTO.setGoogleDriveFileId(bundle.getSongFile().getId());

                // Create song entity
                SongEntity song = songConverter.toEntity(songRequestDTO);

                // Save song
                SongEntity savedSong = songRepository.save(song);

                try {
                    tagService.generateTagsForSong(savedSong);
                } catch (Exception e) {
                    log.warn("Failed to generate tags for song {}: {}", savedSong.getTitle(), e.getMessage());
                }

                importedCount++;

            } catch (Exception e) {
                log.error("Failed to import song {}: {}", bundle.getSongFile().getName(), e.getMessage(), e);
            }
        }

        log.info("Imported {} songs from Google Drive out of {} found", importedCount, songBundles.size());
        return importedCount;
    }


    @Override
    public List<SongDTO> searchSongs(String query, int limit) {
        return sharedSearchService.findSongsByQuery(query, limit);
    }

    @Override
    public List<SongDTO> fetchPopularTracksByArtistId(Long artistId, int limit) {
        try {
            List<SongEntity> songEntities = songRepository.fetchPopularTracksByArtistId(artistId, limit);
            return songEntities.stream().map(songConverter::toDTO).collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }

    }
}

