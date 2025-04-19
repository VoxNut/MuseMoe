package com.javaweb.client.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.javaweb.client.ApiConfig;
import com.javaweb.client.client_service.UserArtistFollowApiClient;
import com.javaweb.model.dto.ArtistDTO;
import com.javaweb.model.dto.UserDTO;
import com.javaweb.utils.Mp3Util;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class UserArtistFollowApiClientImpl implements UserArtistFollowApiClient {

    private final ApiClient apiClient;
    private final UrlEncoder urlEncoder;
    private final ResponseParser responseParser;
    private final ApiConfig apiConfig;
    private final Mp3Util mp3Util;


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
            String responseEntity = apiClient.get(url);
            return responseParser.parseReference(responseEntity,
                    new TypeReference<>() {
                    });

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
