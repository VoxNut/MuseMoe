package com.javaweb.service;

import com.javaweb.model.dto.SongDTO;

import java.util.List;

public interface SearchHistoryService {
    boolean logSearchHistory(Long songId, String searchTerm);

    List<SongDTO> fetchRecentSearchHistory(Integer limit);

    List<String> fetchRecentSearchTerms(Integer limit);

    boolean clearSearchHistoryBySongs(List<Long> songIds);

    boolean clearAllSearchHistory();
}