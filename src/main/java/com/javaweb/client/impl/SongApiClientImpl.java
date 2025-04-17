package com.javaweb.client.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.javaweb.client.ApiConfig;
import com.javaweb.client.client_service.SongApiClient;
import com.javaweb.model.dto.SongDTO;
import com.javaweb.utils.Mp3Util;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
class SongApiClientImpl implements SongApiClient {
    private final ApiClient apiClient;
    private final UrlEncoder urlEncoder;
    private final ResponseParser responseParser;
    private final ApiConfig apiConfig;
    private final Mp3Util mp3Util;


    @Override
    public SongDTO fetchSongByTitle(String title) {
        try {

            String encodedTitle = urlEncoder.encode(title);
            String url = apiConfig.buildSongUrl("/title/" + encodedTitle);
            String responseBody = apiClient.get(url);

            return responseParser.parseObject(responseBody, SongDTO.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<SongDTO> findSongsLike(String title) {
        try {
            String encodedTitle = urlEncoder.encode(title);
            String url = apiConfig.buildSongUrl("/songs_like?title=" + encodedTitle);
            String responseBody = apiClient.get(url);

            List<SongDTO> songDTOS = responseParser.parseReference(
                    responseBody,
                    new TypeReference<>() {
                    }
            );
            songDTOS.forEach(mp3Util::enrichSongDTO);

            return songDTOS;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public SongDTO findSongByUrl(String fileUrl) {
        try {
            String encodedFileUrl = urlEncoder.encode(fileUrl);
            String url = apiConfig.buildSongUrl("/url?songUrl=" + encodedFileUrl);
            String responseBody = apiClient.get(url);
            SongDTO songDTO = responseParser.parseObject(responseBody, SongDTO.class);
            mp3Util.enrichSongDTO(songDTO);
            return songDTO;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<SongDTO> findAllSongs() {
        try {
            String url = apiConfig.buildSongUrl("/all");
            String responseEntity = apiClient.get(url);
            List<SongDTO> songs = responseParser.parseReference(
                    responseEntity
                    , new TypeReference<List<SongDTO>>() {
                    });

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

    @Override
    public List<SongDTO> searchSongs(String query) {
        return List.of();
    }


}