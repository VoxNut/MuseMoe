package com.javaweb.converter;

import com.javaweb.entity.*;
import com.javaweb.enums.MediaType;
import com.javaweb.exception.EntityNotFoundException;
import com.javaweb.model.dto.SongDTO;
import com.javaweb.model.request.SongRequestDTO;
import com.javaweb.repository.AlbumRepository;
import com.javaweb.repository.ArtistRepository;
import com.javaweb.repository.MediaRepository;
import com.javaweb.utils.FileUtil;
import com.javaweb.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class SongConverter implements EntityConverter<SongEntity, SongRequestDTO, SongDTO> {

    private final ModelMapper modelMapper;
    private final AlbumRepository albumRepository;
    private final ArtistRepository artistRepository;
    private final MediaRepository mediaRepository;

    @Override
    public SongDTO toDTO(SongEntity entity) {
        if (entity == null) {
            return null;
        }
        SongDTO dto = modelMapper.map(entity, SongDTO.class);

        dto.setSongTitle(entity.getTitle());
        dto.setAudioFilePath(entity.getAudioFile().getFileUrl());
        if (entity.getAlbum() != null) {
            dto.setAlbum(entity.getAlbum().getTitle());
        }
        if (entity.getArtists() != null) {
            dto.setSongArtist(entity.getArtists().stream()
                    .map(ArtistEntity::getStageName)
                    .collect(Collectors.joining(", ")));
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
            if (request.getFile_url() != null) {
                MediaEntity mediaEntity;

                File file = new File(request.getFile_url());
                if (file.exists()) {

                    mediaEntity = new MediaEntity(request.getFile_url(), MediaType.AUDIO, FileUtil.getFileSize(file));

                    // Extract metadata from file
                    AudioFile audioFile = AudioFileIO.read(file);
                    Tag tag = audioFile.getTag();

                    // Set song metadata from ID3 tags
                    entity.setTitle(getTagValue(tag, FieldKey.TITLE, "Untitled"));

                    try {
                        String year = getTagValue(tag, FieldKey.YEAR, null);
                        entity.setReleaseYear(Integer.valueOf(year));
                    } catch (Exception e) {
                        log.warn("Could not parse year from tag", e);
                    }

                    entity.setLyrics(new LyricsEntity(getTagValue(tag, FieldKey.LYRICS, "")));

                    entity.setDuration(audioFile.getAudioHeader().getTrackLength());

                    entity.setAudioFile(mediaEntity);
                }
            }

            // Initialize fields
            entity.setPlayCount(0);
            entity.setExplicitContent(0);


            if (request.getArtistRequestDTOS() != null) {
                Set<ArtistEntity> artists = request.getArtistRequestDTOS()
                        .stream()
                        .map(artistRequestDTO ->
                                artistRepository.findById(artistRequestDTO.getId())
                                        .orElseGet(null))
                        .collect(Collectors.toSet());
                entity.setArtists(artists);
            }

            if (request.getAlbumRequestDTO() != null) {
                AlbumEntity album = albumRepository.findById(request.getAlbumRequestDTO().getId())
                        .orElseThrow(() -> new EntityNotFoundException("Album Not Found!"));
                entity.setAlbum(album);
            }


            return entity;
        } catch (Exception e) {
            log.error("Error converting DTO to entity", e);
            throw new RuntimeException("Failed to convert song request to entity", e);
        }
    }

    private String getTagValue(Tag tag, FieldKey key, String defaultValue) {
        String value = tag.getFirst(key);
        return StringUtils.isBlank(value) ? defaultValue : value;
    }
}
