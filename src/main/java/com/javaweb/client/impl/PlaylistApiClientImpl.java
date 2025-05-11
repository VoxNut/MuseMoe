package com.javaweb.client.impl;

import com.javaweb.client.ApiConfig;
import com.javaweb.client.client_service.PlaylistApiClient;
import com.javaweb.model.dto.PlaylistDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
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
    public boolean addSongToPlaylist(Long playlistId, Long songId) {
        try {
            String url = apiConfig.buildPlaylistUrl("/add_to_playlist");
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("playlistId", playlistId);
            if (songId != null) {
                requestBody.put("songId", songId);
            }

            Boolean result = apiClient.post(url, requestBody, Boolean.class);

            return result != null && result;
        } catch (Exception e) {
            log.error("Error adding song to playlist: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public PlaylistDTO createPlaylist(String name, Long songId) {
        try {
            String url = apiConfig.buildPlaylistUrl("/create");

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("name", name);
            if (songId != null) {
                requestBody.put("songId", songId);
            }

            PlaylistDTO playlist = apiClient.post(url, requestBody, PlaylistDTO.class);
            return playlist;
        } catch (Exception e) {
            log.error("Error creating playlist: {}", e.getMessage(), e);
            return null;
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
