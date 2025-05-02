package com.javaweb.service.impl;

import com.javaweb.converter.AlbumConverter;
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

    @Override
    public boolean createAlbum(AlbumRequestDTO albumRequestDTO) {
        try {
            boolean res = albumRepository.save(albumConverter.toEntity(albumRequestDTO)) != null;
            return res;
        } catch (Exception e) {
            log.error("Cannot create album: {}", e.getMessage(), e);
            return false;
        }
    }
}
