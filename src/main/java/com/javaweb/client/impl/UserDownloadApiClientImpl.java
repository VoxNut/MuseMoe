package com.javaweb.client.impl;

import com.javaweb.client.ApiConfig;
import com.javaweb.client.client_service.UserDownloadApiClient;
import com.javaweb.model.dto.SongDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
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

    @Override
    public boolean createUserDownload(SongDTO song) {
        try {
            String url = apiConfig.buildUserDownloadUrl("/create");
            apiClient.post(url, song, Boolean.class);
            return true;
        } catch (Exception e) {
            log.error("Failed to create user download with song id: {} ", song.getId());
            return false;
        }
    }
}
