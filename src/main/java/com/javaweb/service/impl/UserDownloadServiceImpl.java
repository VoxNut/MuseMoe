package com.javaweb.service.impl;

import com.javaweb.converter.SongConverter;
import com.javaweb.entity.UserDownloadEntity;
import com.javaweb.model.dto.SongDTO;
import com.javaweb.repository.UserDownloadRepository;
import com.javaweb.service.UserDownloadService;
import com.javaweb.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserDownloadServiceImpl implements UserDownloadService {
    private final UserDownloadRepository userDownloadRepository;

    private final SongConverter songConverter;

    @Override
    public List<SongDTO> findAllDownloadedSongs() {
        try {
            List<SongDTO> songDTOS = userDownloadRepository
                    .findByUserId(Objects.requireNonNull(SecurityUtils.getPrincipal()).getId())
                    .stream()
                    .map(UserDownloadEntity::getSong)
                    .map(songConverter::toDTO)
                    .collect(Collectors.toList());
            return songDTOS;
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }

    }
}
