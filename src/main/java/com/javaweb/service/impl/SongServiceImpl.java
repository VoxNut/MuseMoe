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
        return songRepository.findAllSongsLike(title)
                .stream()
                .map(songConverter::toDTO)
                .collect(Collectors.toList());
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
        return songRepository.findSongByUrl(songUrl)
                .map(songConverter::toDTO)
                .orElseThrow(() -> new EntityNotFoundException("Song with URL " + songUrl + " not found"));
    }

    @Override
    public List<SongDTO> findAllSongs() {
        return songRepository.findAll()
                .stream()
                .map(songConverter::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public boolean createSong(SongRequestDTO songRequestDTO) {
        SongEntity song = songConverter.toEntity(songRequestDTO);
        try {
            SongEntity res = songRepository.save(song);
            tagService.generateTagsForSong(res);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
