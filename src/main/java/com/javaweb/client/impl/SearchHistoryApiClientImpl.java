package com.javaweb.client.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.javaweb.client.ApiConfig;
import com.javaweb.client.client_service.SearchHistoryApiClient;
import com.javaweb.model.dto.SongDTO;
import com.javaweb.utils.Mp3Util;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class SearchHistoryApiClientImpl implements SearchHistoryApiClient {

    private final ApiClient apiClient;
    private final UrlEncoder urlEncoder;
    private final ResponseParser responseParser;
    private final ApiConfig apiConfig;
    private final Mp3Util mp3Util;

    @Override
    public Boolean logSearchHistory(Long songId, String searchTerm) {
        try {
            String url = apiConfig.buildSearchHistoryUrl("/log");
            searchTerm = urlEncoder.encode(searchTerm);
            Map<String, Object> params = new HashMap<>();
            params.put("songId", songId);
            params.put("searchTerm", searchTerm);

            String responseEntity = apiClient.postWithFormParams(url, params);
            return responseParser.parseObject(responseEntity, Boolean.class);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<SongDTO> findRecentSearchHistory(int limit) {
        try {
            String url = apiConfig.buildSearchHistoryUrl("/recent_songs?limit=" + limit);
            String responseBody = apiClient.get(url);
            List<SongDTO> recentSearches = responseParser.parseReference(responseBody,
                    new TypeReference<>() {
                    });

            recentSearches.forEach(mp3Util::enrichSongDTO);
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
            String responseBody = apiClient.get(url);
            return responseParser.parseReference(responseBody, new TypeReference<>() {
            });
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public Boolean clearSearchHistoryBySongs(List<Long> songIds) {
        try {
            String url = apiConfig.buildSearchHistoryUrl("/clear-songs");
            String jsonBody = responseParser.writeValueAsString(songIds);
            String responseBody = apiClient.deleteWithBody(url, jsonBody);
            return responseParser.parseObject(responseBody, Boolean.class);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Boolean clearAllSearchHistory() {
        try {
            String url = apiConfig.buildSearchHistoryUrl("/clear-all");
            String responseBody = apiClient.delete(url);
            return responseParser.parseObject(responseBody, Boolean.class);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}