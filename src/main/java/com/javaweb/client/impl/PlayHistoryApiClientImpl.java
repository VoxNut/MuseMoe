package com.javaweb.client.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.javaweb.client.ApiConfig;
import com.javaweb.client.client_service.PlayHistoryApiClient;
import com.javaweb.model.dto.SongDTO;
import com.javaweb.utils.Mp3Util;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class PlayHistoryApiClientImpl implements PlayHistoryApiClient {

    private final ApiClient apiClient;
    private final UrlEncoder urlEncoder;
    private final ResponseParser responseParser;
    private final ApiConfig apiConfig;
    private final Mp3Util mp3Util;


    @Override
    public Boolean createNewPlayHistory(Long songId) {
        try {
            String url = apiConfig.buildPlayHistoryUrl("/create");
            String responseEntity = apiClient.postWithFormParam(url, "songId", songId);
            return responseParser.parseObject(responseEntity, Boolean.class);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<SongDTO> findRecentPlayHistory(int limit) {
        try {
            String url = apiConfig.buildPlayHistoryUrl("/recent_songs?limit=" + limit);
            String responseBody = apiClient.get(url);
            List<SongDTO> recentSongs = responseParser.parseReference(responseBody,
                    new TypeReference<>() {
                    });
            recentSongs = recentSongs.stream()
                    .distinct()
                    .toList();

            recentSongs.forEach(mp3Util::enrichSongDTO);

            return recentSongs;

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
