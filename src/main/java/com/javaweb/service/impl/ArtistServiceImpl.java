package com.javaweb.service.impl;

import com.javaweb.converter.ArtistConverter;
import com.javaweb.model.request.ArtistRequestDTO;
import com.javaweb.repository.ArtistRepository;
import com.javaweb.service.ArtistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArtistServiceImpl implements ArtistService {
    private final ArtistConverter artistConverter;
    private final ArtistRepository artistRepository;

    @Override
    public boolean createArtist(ArtistRequestDTO artistRequestDTO) {
        try {
            return artistRepository.save(artistConverter.toEntity(artistRequestDTO)) != null;
        } catch (Exception e) {
            log.error("Cannot create artist: {}", e.getMessage(), e);
            return false;
        }
    }


}
