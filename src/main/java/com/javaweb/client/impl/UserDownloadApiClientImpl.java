package com.javaweb.client.impl;

import com.javaweb.client.ApiConfig;
import com.javaweb.client.client_service.UserDownloadApiClient;
import com.javaweb.model.dto.SongDTO;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class UserDownloadApiClientImpl implements UserDownloadApiClient {

    private final ApiClient apiClient;
    private final ApiConfig apiConfig;


    @Override
    public List<SongDTO> findUserDownloadedSongs() {
        try {
            String url = apiConfig.buildUserDownloadUrl("/songs");
            List<SongDTO> songs = apiClient.getList(url, SongDTO.class);
            return songs;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
