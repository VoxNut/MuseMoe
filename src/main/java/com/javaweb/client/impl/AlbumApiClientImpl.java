package com.javaweb.client.impl;

import com.javaweb.client.ApiConfig;
import com.javaweb.client.client_service.AlbumApiClient;
import com.javaweb.model.dto.AlbumDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class AlbumApiClientImpl implements AlbumApiClient {
    private final ApiClient apiClient;
    private final UrlEncoder urlEncoder;
    private final ApiConfig apiConfig;


    @Override
    public List<AlbumDTO> searchAlbums(String query, int limit) {
        try {
            String url = apiConfig.buildAlbumsUrl("/search?query=" + query + "&limit=" + limit);
            return apiClient.getList(url, AlbumDTO.class);
        } catch (Exception e) {
            log.error("Error searching albums: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}
