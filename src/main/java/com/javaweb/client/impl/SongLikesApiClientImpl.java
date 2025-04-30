package com.javaweb.client.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.javaweb.client.ApiConfig;
import com.javaweb.client.client_service.SongLikesApiClient;
import com.javaweb.model.dto.SongLikesDTO;
import com.javaweb.utils.Mp3Util;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;


@RequiredArgsConstructor

public class SongLikesApiClientImpl implements SongLikesApiClient {
    private final ApiClient apiClient;
    private final UrlEncoder urlEncoder;
    private final ResponseParser responseParser;
    private final ApiConfig apiConfig;

    private final Mp3Util mp3Util;


    @Override
    public Boolean createNewSongLikes(Long songId) {
        try {
            String url = apiConfig.buildSongLikesUrl("/song_id");
            String response = apiClient.postWithFormParam(url, "songId", songId);
            return Boolean.parseBoolean(response);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Boolean checkSongLiked(Long songId) {
        try {
            String url = apiConfig.buildSongLikesUrl("/song_liked?songId=" + songId);
            String responseEntity = apiClient.get(url);
            return responseParser.parseObject(responseEntity, Boolean.class);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Boolean deleteSongLikes(Long songId) {
        try {
            var url = apiConfig.buildSongLikesUrl("/delete/") + songId;
            var responseEntity = apiClient.delete(url);
            return responseParser.parseObject(responseEntity, Boolean.class);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<SongLikesDTO> findAllSongLikesByUser() {
        try {
            var url = apiConfig.buildSongLikesUrl("/all");
            var responseEntity = apiClient.get(url);
            List<SongLikesDTO> songLikesDTOS = responseParser.parseReference(responseEntity, new TypeReference<>() {
            });

            songLikesDTOS.forEach(songLike -> {
                mp3Util.enrichSongDTO(songLike.getSongDTO());
            });
            return songLikesDTOS;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
