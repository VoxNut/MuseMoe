package com.javaweb.service.impl;

import com.javaweb.converter.PlaylistConverter;
import com.javaweb.model.dto.PlaylistDTO;
import com.javaweb.repository.PlaylistRepository;
import com.javaweb.service.PlaylistService;
import com.javaweb.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class PlaylistServiceImpl implements PlaylistService {

    private final PlaylistRepository playlistRepository;

    private final PlaylistConverter playlistConverter;

    @Override
    public List<PlaylistDTO> findPlaylistsByUserId() {
        List<PlaylistDTO> res = playlistRepository.findPlaylistsByUserId(
                        Objects.requireNonNull(SecurityUtils.getPrincipal()).getId()
                )
                .stream()
                .map(playlistConverter::toDTO)
                .collect(Collectors.toList());
        return res;
    }

    @Override
    public List<PlaylistDTO> findAllPlaylists() {
        List<PlaylistDTO> res = playlistRepository.findAll()
                .stream()
                .map(playlistConverter::toDTO)
                .collect(Collectors.toList());
        return res;
    }
}
