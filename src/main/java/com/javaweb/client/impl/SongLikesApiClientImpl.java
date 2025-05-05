package com.javaweb.client.impl;

import com.javaweb.client.ApiConfig;
import com.javaweb.client.client_service.SongLikesApiClient;
import com.javaweb.model.dto.SongLikesDTO;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;


@RequiredArgsConstructor

public class SongLikesApiClientImpl implements SongLikesApiClient {
    private final ApiClient apiClient;
    private final UrlEncoder urlEncoder;
    private final ApiConfig apiConfig;


    @Override
    public Boolean createNewSongLikes(Long songId) {
        try {
            String url = apiConfig.buildSongLikesUrl("/song_id");
            return apiClient.post(url,
                    SongLikesDTO.builder()
                            .songId(songId)
                            .build()
                    , Boolean.class);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Boolean checkSongLiked(Long songId) {
        try {
            String url = apiConfig.buildSongLikesUrl("/song_liked?songId=" + songId);
            return apiClient.get(url, Boolean.class);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Boolean deleteSongLikes(Long songId) {
        try {
            var url = apiConfig.buildSongLikesUrl("/delete/") + songId;
            return apiClient.delete(url, Boolean.class);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<SongLikesDTO> findAllSongLikesByUser() {
        try {
            var url = apiConfig.buildSongLikesUrl("/all");
            List<SongLikesDTO> songLikesDTOS = apiClient.getList(url, SongLikesDTO.class);
            return songLikesDTOS;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
