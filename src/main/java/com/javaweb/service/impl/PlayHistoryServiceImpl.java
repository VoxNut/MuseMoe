package com.javaweb.service.impl;

import com.javaweb.converter.SongConverter;
import com.javaweb.entity.PlayHistoryEntity;
import com.javaweb.entity.SongEntity;
import com.javaweb.entity.UserEntity;
import com.javaweb.model.dto.SongDTO;
import com.javaweb.repository.PlayHistoryRepository;
import com.javaweb.repository.SongRepository;
import com.javaweb.repository.UserRepository;
import com.javaweb.service.PlayHistoryService;
import com.javaweb.utils.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.cache.CachesEndpoint;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
public class PlayHistoryServiceImpl implements PlayHistoryService {

    private final PlayHistoryRepository playHistoryRepository;
    private final SongRepository songRepository;
    private final UserRepository userRepository;
    private final CachesEndpoint cachesEndpoint;
    private final SongConverter songConverter;

    @Override
    public boolean createNewPlayHistory(Long songId) {
        try {
            SongEntity song = songRepository.findById(songId)
                    .orElseThrow(() -> new EntityNotFoundException("Song with id: " + songId + " not found"));
            song.incrementPlayCount();
            UserEntity user = userRepository.findById(
                            Objects.requireNonNull(SecurityUtils.getPrincipal()).getId())
                    .orElseThrow(() -> new EntityNotFoundException("User not found!"));
            PlayHistoryEntity playHistoryEntity = new PlayHistoryEntity(user, song);
            playHistoryRepository.save(playHistoryEntity);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    @Override
    public List<SongDTO> fetchRecentPlayHistory(Integer limit) {
        Long userId = Objects.requireNonNull(SecurityUtils.getPrincipal()).getId();
        try {
            List<SongDTO> recentSongs = playHistoryRepository.fetchRecentPlayHistory(userId, limit)
                    .stream()
                    .map(PlayHistoryEntity::getSong)
                    .map(songConverter::toDTO)
                    .toList();
            return recentSongs;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @Override
    public boolean clearPlayHistoryBySongs(List<Long> songIds) {
        try {
            Long userId = Objects.requireNonNull(SecurityUtils.getPrincipal()).getId();
            int deletedCount = playHistoryRepository.deletePlayHistoryBySongIds(userId, songIds);
            return deletedCount > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean clearAllPlayHistory() {
        try {
            Long userId = Objects.requireNonNull(SecurityUtils.getPrincipal()).getId();
            int deletedCount = playHistoryRepository.deleteAllPlayHistoryByUserId(userId);
            return deletedCount > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
