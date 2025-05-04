package com.javaweb.client.impl;

import com.javaweb.client.ApiConfig;
import com.javaweb.client.client_service.PlaylistApiClient;
import com.javaweb.model.dto.PlaylistDTO;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class PlaylistApiClientImpl implements PlaylistApiClient {
    private final ApiClient apiClient;
    private final UrlEncoder urlEncoder;
    private final ApiConfig apiConfig;


    @Override
    public List<PlaylistDTO> findPlaylistByUserId() {
        try {

            String url = apiConfig.buildPlaylistUrl("");
            List<PlaylistDTO> playlistDTOS = apiClient.getList(url, PlaylistDTO.class);
            return playlistDTOS;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public List<PlaylistDTO> findAllPlaylists() {
        try {
            String url = apiConfig.buildPlaylistUrl("/all");
            List<PlaylistDTO> playlistDTOS = apiClient.getList(url, PlaylistDTO.class);
            return playlistDTOS;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
