package com.javaweb.client.client_service;

import com.javaweb.model.dto.SongDTO;

import java.util.List;

public interface SearchHistoryApiClient {
    Boolean logSearchHistory(Long songId, String searchTerm);

    List<SongDTO> findRecentSearchHistory(int limit);

    List<String> findRecentSearchTerms(int limit);

    Boolean clearSearchHistoryBySongs(List<Long> songIds);

    Boolean clearAllSearchHistory();
}
