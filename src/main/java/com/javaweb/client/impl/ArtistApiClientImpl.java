package com.javaweb.client.impl;

import com.javaweb.client.ApiConfig;
import com.javaweb.client.client_service.ArtistApiClient;
import com.javaweb.model.dto.ArtistDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class ArtistApiClientImpl implements ArtistApiClient {

    private final ApiClient apiClient;
    private final ApiConfig apiConfig;


    @Override
    public List<ArtistDTO> searchArtists(String query, int limit) {
        try {
            String url = apiConfig.buildArtistsUrl("/search?query=" + query + "&limit=" + limit);
            return apiClient.getList(url, ArtistDTO.class);
        } catch (Exception e) {
            log.error("Error searching artists: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}
