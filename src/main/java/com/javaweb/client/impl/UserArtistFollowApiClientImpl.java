package com.javaweb.client.impl;

import com.javaweb.client.ApiConfig;
import com.javaweb.client.client_service.UserArtistFollowApiClient;
import com.javaweb.model.dto.ArtistDTO;
import com.javaweb.model.dto.UserDTO;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class UserArtistFollowApiClientImpl implements UserArtistFollowApiClient {

    private final ApiClient apiClient;
    private final UrlEncoder urlEncoder;
    private final ApiConfig apiConfig;


    @Override
    public List<UserDTO> findFollowersByArtistId(Long artistId) {
        return List.of();
    }

    @Override
    public Boolean followArtist(Long artistId) {
        return null;
    }

    @Override
    public Boolean unfollowArtist(Long artistId) {
        return null;
    }

    @Override
    public List<ArtistDTO> findFollowedArtists() {
        try {
            String url = apiConfig.buildFollowsUrl("/artists");
            return apiClient.getList(url, ArtistDTO.class);

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
