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

import java.util.Collections;
import java.util.List;
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
    public boolean createSong(SongRequestDTO songRequestDTO) {
        try {

            songRequestDTO.setGoogleDriveFileId(
                    googleDriveService.uploadSongFile(songRequestDTO.getMp3File()));

            SongEntity song = songConverter.toEntity(songRequestDTO);
            SongEntity res = songRepository.save(song);
            tagService.generateTagsForSong(res);
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
}
