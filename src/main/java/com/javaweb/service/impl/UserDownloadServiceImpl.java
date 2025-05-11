package com.javaweb.service.impl;

import com.javaweb.converter.SongConverter;
import com.javaweb.entity.SongEntity;
import com.javaweb.entity.UserDownloadEntity;
import com.javaweb.entity.UserEntity;
import com.javaweb.exception.EntityNotFoundException;
import com.javaweb.model.dto.SongDTO;
import com.javaweb.repository.SongRepository;
import com.javaweb.repository.UserDownloadRepository;
import com.javaweb.repository.UserRepository;
import com.javaweb.service.UserDownloadService;
import com.javaweb.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

    private final UserRepository userRepository;
    private final SongRepository songRepository;

    @Override
    public boolean createNewUserDownload(Long songId) {
        try {
            UserEntity user = userRepository.findById(SecurityUtils.getPrincipal().getId())
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));

            SongEntity song = songRepository.findById(songId)
                    .orElseThrow(() -> new EntityNotFoundException("Song not found"));

            if (userDownloadRepository.existsByUserIdAndSongId(user.getId(), songId)) {
                UserDownloadEntity download = userDownloadRepository.findByUserIdAndSongId(user.getId(), songId);
                download.setDownloadDate(LocalDateTime.now());
                userDownloadRepository.save(download);
            } else {
                UserDownloadEntity download = new UserDownloadEntity(user, song);
                userDownloadRepository.save(download);
            }

            return true;
        } catch (Exception e) {
            log.error("Error creating user download", e);
            return false;
        }
    }

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
