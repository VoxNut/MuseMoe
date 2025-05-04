package com.javaweb.client.impl;

import com.javaweb.client.ApiConfig;
import com.javaweb.client.client_service.PlayHistoryApiClient;
import com.javaweb.model.dto.PlayHistoryDTO;
import com.javaweb.model.dto.SongDTO;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class PlayHistoryApiClientImpl implements PlayHistoryApiClient {

    private final ApiClient apiClient;
    private final UrlEncoder urlEncoder;
    private final ApiConfig apiConfig;


    @Override
    public Boolean createNewPlayHistory(Long songId) {
        try {
            String url = apiConfig.buildPlayHistoryUrl("/create");
            return apiClient.post(url,
                    PlayHistoryDTO.builder()
                            .songId(songId)
                            .build()
                    , Boolean.class);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<SongDTO> findRecentPlayHistory(int limit) {
        try {
            String url = apiConfig.buildPlayHistoryUrl("/recent_songs?limit=" + limit);
            List<SongDTO> recentSongs = apiClient.getList(url, SongDTO.class);
            return recentSongs;

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public Boolean clearPlayHistoryBySongs(List<Long> songIds) {
        try {
            String url = apiConfig.buildPlayHistoryUrl("/clear-songs");
            return apiClient.delete(url, songIds, Boolean.class);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Boolean clearAllPlayHistory() {
        try {
            String url = apiConfig.buildPlayHistoryUrl("/clear-all");
            return apiClient.delete(url, Boolean.class);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
