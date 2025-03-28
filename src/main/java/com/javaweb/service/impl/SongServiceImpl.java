package com.javaweb.service.impl;


import com.javaweb.converter.SongConverter;
import com.javaweb.exception.EntityNotFoundException;
import com.javaweb.model.dto.SongDTO;
import com.javaweb.repository.SongRepository;
import com.javaweb.service.SongService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class SongServiceImpl implements SongService {


    private final SongRepository songRepository;

    private final SongConverter songConverter;

    private final Logger logger = LoggerFactory.getLogger(SongServiceImpl.class);
    private final LocalContainerEntityManagerFactoryBean entityManagerFactory;
    private final ModelMapper modelMapper;

    @Override
    public SongDTO findOneByTitle(String title) {
        return songConverter.toDTO(
                songRepository.findOneByTitle(title)
                        .orElseThrow(() -> new EntityNotFoundException("Song not found!"))
        );
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
                            logger.warn("Song with id: {} not found", id);
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
}
