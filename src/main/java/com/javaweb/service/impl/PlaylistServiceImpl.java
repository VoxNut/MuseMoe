package com.javaweb.service.impl;

import com.javaweb.converter.PlaylistConverter;
import com.javaweb.model.dto.PlaylistDTO;
import com.javaweb.repository.PlaylistRepository;
import com.javaweb.service.PlaylistService;
import com.javaweb.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PlaylistServiceImpl implements PlaylistService {

    private final PlaylistRepository playlistRepository;

    private final PlaylistConverter playlistConverter;

    @Override
    public List<PlaylistDTO> findPlaylistsByUserId() {
        try {
            List<PlaylistDTO> res = playlistRepository
                    .findPlaylistsByUserIdWithSongsOrdered(Objects.requireNonNull(SecurityUtils.getPrincipal()).getId())
                    .stream()
                    .map(playlistConverter::toDTO)
                    .collect(Collectors.toList());
            log.info("Successfully creating playlist list for user with ID: '{}'", Objects.requireNonNull(SecurityUtils.getPrincipal()).getId());
            return res;
        } catch (Exception e) {
            log.error("Failed to create playlist list for user with ID: '{}'", Objects.requireNonNull(SecurityUtils.getPrincipal()).getId(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<PlaylistDTO> findAllPlaylists() {
        try {
            List<PlaylistDTO> res = playlistRepository.findAll()
                    .stream()
                    .map(playlistConverter::toDTO)
                    .collect(Collectors.toList());
            return res;
        } catch (Exception e) {
            return Collections.emptyList();
        }

    }

}
