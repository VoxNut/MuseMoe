package com.javaweb.client.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.javaweb.client.ApiConfig;
import com.javaweb.client.client_service.PlaylistApiClient;
import com.javaweb.model.dto.PlaylistDTO;
import com.javaweb.utils.Mp3Util;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class PlaylistApiClientImpl implements PlaylistApiClient {
    private final ApiClient apiClient;
    private final UrlEncoder urlEncoder;
    private final ResponseParser responseParser;
    private final ApiConfig apiConfig;
    private final Mp3Util mp3Util;


    @Override
    public List<PlaylistDTO> findPlaylistByUserId() {
        try {

            String url = apiConfig.buildPlaylistUrl("");
            String responseEntity = apiClient.get(url);

            List<PlaylistDTO> playlistDTOS = responseParser.parseReference(
                    responseEntity,
                    new TypeReference<>() {
                    });

            playlistDTOS.stream()
                    .flatMap(playlistDTO -> playlistDTO.getSongs().stream())
                    .forEach(mp3Util::enrichSongDTO);

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
            String responseEntity = apiClient.get(url);
            return responseParser.parseReference(
                    responseEntity,
                    new TypeReference<List<PlaylistDTO>>() {
                    }
            );
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
