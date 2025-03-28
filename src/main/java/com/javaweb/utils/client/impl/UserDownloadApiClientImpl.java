package com.javaweb.utils.client.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.javaweb.model.dto.SongDTO;
import com.javaweb.utils.Mp3Util;
import com.javaweb.utils.client.ApiConfig;
import com.javaweb.utils.client.client_service.UserDownloadApiClient;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class UserDownloadApiClientImpl implements UserDownloadApiClient {

    private final ApiClient apiClient;
    private final UrlEncoder urlEncoder;
    private final ResponseParser responseParser;
    private final ApiConfig apiConfig;
    private final Mp3Util mp3Util;


    @Override
    public List<SongDTO> findUserDownloadedSongs() {
        try {
            String url = apiConfig.buildUserDownloadUrl("/songs");
            String responseEntity = apiClient.get(url);
            List<SongDTO> songs = responseParser.parseReference(
                    responseEntity,
                    new TypeReference<List<SongDTO>>() {
                    }
            );
            if (songs != null) {
                for (SongDTO song : songs) {
                    mp3Util.enrichSongDTO(song);
                }
            }
            return songs;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
