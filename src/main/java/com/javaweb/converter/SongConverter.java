package com.javaweb.converter;

import com.google.api.services.drive.model.File;
import com.javaweb.entity.*;
import com.javaweb.model.dto.SongDTO;
import com.javaweb.model.request.SongRequestDTO;
import com.javaweb.repository.AlbumRepository;
import com.javaweb.repository.ArtistRepository;
import com.javaweb.service.StreamingMediaService;
import com.javaweb.service.impl.GoogleDriveService;
import com.javaweb.utils.SecurityUtils;
import com.javaweb.utils.StreamingAudioPlayer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class SongConverter implements EntityConverter<SongEntity, SongRequestDTO, SongDTO> {


    private final ModelMapper modelMapper;
    private final AlbumRepository albumRepository;
    private final ArtistRepository artistRepository;
    private final GoogleDriveService googleDriveService;
    private final StreamingAudioPlayer streamingPlayer;
    private final StreamingMediaService streamingMediaService;


    @Override
    public SongDTO toDTO(SongEntity entity) {
        if (entity == null) {
            return null;
        }
        SongDTO dto = modelMapper.map(entity, SongDTO.class);

        if (entity.getStreamingMedia() != null) {
            dto.setDriveFileId(entity.getStreamingMedia().getGoogleDriveId());
            dto.setWebContentLink(entity.getStreamingMedia().getWebContentLink());
        }

        if (entity.getAlbum() != null) {
            dto.setSongAlbum(entity.getAlbum().getTitle());
            dto.setAlbumId(entity.getAlbum().getId());
            StreamingMediaEntity coverArt = entity.getAlbum().getCoverArt();
            if (entity.getAlbum().getCoverArt() != null) {
                dto.setAlbumArtId(coverArt.getGoogleDriveId());
            }
        }

        if (entity.getArtists() != null) {
            dto.setSongArtist(entity.getArtists().stream()
                    .map(ArtistEntity::getStageName)
                    .collect(Collectors.joining(", ")));

            dto.setArtistIds(entity.getArtists().stream()
                    .map(ArtistEntity::getId)
                    .toList());
        }

        if (entity.getFrame() != null && entity.getDuration() != null) {
            dto.setSongLength(formatDuration(dto));
            dto.setLengthInMilliseconds(dto.getDuration() * 1000);
            dto.setFrameRatePerMilliseconds(getFrameRatePerMilliseconds(dto));
        }

        return dto;
    }


    @Override
    public SongEntity toEntity(SongRequestDTO request) {
        if (request == null) {
            return null;
        }

        try {
            SongEntity entity = new SongEntity();

            // Handle the audio file
            if (request.getGoogleDriveFileId() != null) {

                File driveFile = googleDriveService.getFileMetadata(request.getGoogleDriveFileId());

                StreamingMediaEntity mediaEntity = streamingMediaService.getOrCreateStreamingMedia(
                        driveFile.getId(),
                        driveFile.getName(),
                        driveFile.getMimeType(),
                        driveFile.getSize(),
                        driveFile.getWebContentLink()
                );

                entity.setStreamingMedia(mediaEntity);

                SongDTO tempSongDTO = new SongDTO();
                tempSongDTO.setDriveFileId(driveFile.getId());

                try {
                    streamingPlayer.extractMetadata(tempSongDTO);

                    entity.setTitle(tempSongDTO.getTitle());

                    if (tempSongDTO.getSongLyrics() != null && !tempSongDTO.getSongLyrics().isEmpty()) {
                        entity.setLyrics(new LyricsEntity(tempSongDTO.getSongLyrics()));
                    } else {
                        entity.setLyrics(new LyricsEntity(""));
                    }

                    entity.setDuration(tempSongDTO.getDuration());
                    entity.setReleaseYear(tempSongDTO.getReleaseYear());
                    entity.setBitrate(tempSongDTO.getBitrate());
                    entity.setFrame(tempSongDTO.getFrame());
                } catch (Exception e) {
                    log.warn("Could not extract full metadata from Google Drive file", e);
                    entity.setTitle(driveFile.getName());
                    entity.setDuration(0);
                }
            }

            // Initialize fields
            entity.setPlayCount(0);
            entity.setExplicitContent(0);


            List<ArtistEntity> artists = artistRepository.findAllById(request.getArtistIds());
            entity.setArtists(new HashSet<>(artists));

            AlbumEntity album = albumRepository.findById(request.getAlbumId())
                    .orElseGet(
                            () -> AlbumEntity.builder()
                                    .title(entity.getTitle())
                                    .artist(artistRepository
                                            .findByUserId(Objects.requireNonNull(SecurityUtils.getPrincipal().getId()))
                                            .orElseGet(null))
                                    .releaseYear(entity.getReleaseYear())
                                    .coverArt(entity.getStreamingMedia())
                                    .build());

            entity.setAlbum(album);


            return entity;
        } catch (Exception e) {
            log.error("Error converting DTO to entity", e);
            throw new RuntimeException("Failed to convert song request to entity", e);
        }
    }

    private String formatDuration(SongDTO songDTO) {
        long minutes = songDTO.getDuration() / 60;
        long remainingSeconds = songDTO.getDuration() % 60;
        return String.format("%02d:%02d", minutes, remainingSeconds);
    }

    private double getFrameRatePerMilliseconds(SongDTO songDTO) {
        return (double) songDTO.getFrame() / songDTO.getLengthInMilliseconds();
    }


}
