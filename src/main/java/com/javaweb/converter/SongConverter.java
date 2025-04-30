package com.javaweb.converter;

import com.javaweb.entity.ArtistEntity;
import com.javaweb.entity.MediaEntity;
import com.javaweb.entity.SongEntity;
import com.javaweb.entity.UserEntity;
import com.javaweb.enums.MediaType;
import com.javaweb.model.dto.SongDTO;
import com.javaweb.model.request.SongRequestDTO;
import com.javaweb.repository.UserRepository;
import com.javaweb.utils.FileUtil;
import com.javaweb.utils.SecurityUtils;
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
import java.text.SimpleDateFormat;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class SongConverter implements EntityConverter<SongEntity, SongRequestDTO, SongDTO> {

    private final ModelMapper modelMapper;
    private final UserRepository userRepository;

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
                MediaEntity mediaEntity = new MediaEntity();
                mediaEntity.setFileUrl(request.getFile_url());
                mediaEntity.setFileType(MediaType.AUDIO);

                File file = new File(request.getFile_url());
                if (file.exists()) {
                    mediaEntity.setFileSize(FileUtil.getFileSize(file));

                    // Extract metadata from file
                    AudioFile audioFile = AudioFileIO.read(file);
                    Tag tag = audioFile.getTag();

                    // Set song metadata from ID3 tags
                    entity.setTitle(getTagValue(tag, FieldKey.TITLE, "Untitled"));

                    try {
                        String year = getTagValue(tag, FieldKey.YEAR, null);
                        if (year != null && !year.isEmpty()) {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
                            entity.setReleaseDate(sdf.parse(year));
                        }
                    } catch (Exception e) {
                        log.warn("Could not parse year from tag", e);
                    }

                    entity.setDuration(audioFile.getAudioHeader().getTrackLength());

                    entity.setAudioFile(mediaEntity);
                }
            }

            // Initialize fields
            entity.setPlayCount(0);
            entity.setExplicitContent(0);

            //Find Artist
            Long userId = SecurityUtils.getPrincipal().getId();
            UserEntity user = userRepository.findById(userId)
                    .orElseGet(null);

            if (user != null) {
                ArtistEntity artist = user.getArtist();
                entity.setArtists(Set.of(artist));
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
