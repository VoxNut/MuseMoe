package com.javaweb.service.impl;

import com.javaweb.converter.SongConverter;
import com.javaweb.entity.SearchHistoryEntity;
import com.javaweb.entity.SongEntity;
import com.javaweb.entity.UserEntity;
import com.javaweb.model.dto.SongDTO;
import com.javaweb.repository.SearchHistoryRepository;
import com.javaweb.repository.SongRepository;
import com.javaweb.repository.UserRepository;
import com.javaweb.service.SearchHistoryService;
import com.javaweb.utils.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
public class SearchHistoryServiceImpl implements SearchHistoryService {

    private final SearchHistoryRepository searchHistoryRepository;
    private final SongRepository songRepository;
    private final UserRepository userRepository;
    private final SongConverter songConverter;

    @Override
    public boolean logSearchHistory(Long songId, String searchTerm) {
        try {
            SongEntity song = songRepository.findById(songId)
                    .orElseThrow(() -> new EntityNotFoundException("Song with id: " + songId + " not found"));
            UserEntity user = userRepository.findById(
                            Objects.requireNonNull(SecurityUtils.getPrincipal()).getId())
                    .orElseThrow(() -> new EntityNotFoundException("User not found!"));
            SearchHistoryEntity searchHistoryEntity = new SearchHistoryEntity(user, song, searchTerm);
            searchHistoryRepository.save(searchHistoryEntity);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<SongDTO> fetchRecentSearchHistory(Integer limit) {
        Long userId = Objects.requireNonNull(SecurityUtils.getPrincipal()).getId();
        try {
            return searchHistoryRepository.fetchRecentSearchHistory(userId, limit)
                    .stream()
                    .map(SearchHistoryEntity::getSong)
                    .map(songConverter::toDTO)
                    .toList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public List<String> fetchRecentSearchTerms(Integer limit) {
        Long userId = Objects.requireNonNull(SecurityUtils.getPrincipal()).getId();
        try {
            return searchHistoryRepository.fetchRecentSearchTerms(userId, limit);
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public boolean clearSearchHistoryBySongs(List<Long> songIds) {
        try {
            Long userId = Objects.requireNonNull(SecurityUtils.getPrincipal()).getId();
            int deletedCount = searchHistoryRepository.deleteSearchHistoryBySongIds(userId, songIds);
            return deletedCount > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean clearAllSearchHistory() {
        try {
            Long userId = Objects.requireNonNull(SecurityUtils.getPrincipal()).getId();
            int deletedCount = searchHistoryRepository.deleteAllSearchHistoryByUserId(userId);
            return deletedCount > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}