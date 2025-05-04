package com.javaweb.client.impl;

import com.javaweb.client.ApiConfig;
import com.javaweb.client.client_service.SearchHistoryApiClient;
import com.javaweb.model.dto.SearchHistoryDTO;
import com.javaweb.model.dto.SongDTO;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class SearchHistoryApiClientImpl implements SearchHistoryApiClient {

    private final ApiClient apiClient;
    private final UrlEncoder urlEncoder;
    private final ApiConfig apiConfig;

    @Override
    public Boolean logSearchHistory(Long songId, String searchTerm) {
        try {
            String url = apiConfig.buildSearchHistoryUrl("/log");
            searchTerm = urlEncoder.encode(searchTerm);
            return apiClient.post(url, new SearchHistoryDTO(songId, searchTerm), Boolean.class);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<SongDTO> findRecentSearchHistory(int limit) {
        try {
            String url = apiConfig.buildSearchHistoryUrl("/recent_songs?limit=" + limit);
            List<SongDTO> recentSearches = apiClient.getList(url, SongDTO.class);
            return recentSearches;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public List<String> findRecentSearchTerms(int limit) {
        try {
            String url = apiConfig.buildSearchHistoryUrl("/recent_terms?limit=" + limit);
            return apiClient.getList(url, String.class);
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public Boolean clearSearchHistoryBySongs(List<Long> songIds) {
        try {
            String url = apiConfig.buildSearchHistoryUrl("/clear-songs");
            return apiClient.delete(url, songIds, Boolean.class);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Boolean clearAllSearchHistory() {
        try {
            String url = apiConfig.buildSearchHistoryUrl("/clear-all");
            return apiClient.delete(url, Boolean.class);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}