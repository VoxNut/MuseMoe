package com.javaweb.client.impl;

import com.javaweb.client.ApiConfig;
import com.javaweb.client.client_service.AlbumApiClient;
import com.javaweb.model.dto.AlbumDTO;
import com.javaweb.model.request.AlbumRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Override
    public AlbumDTO getAlbumById(Long albumId) {
        try {
            String url = apiConfig.buildAlbumsUrl("/album?albumId=" + albumId);
            return apiClient.get(url, AlbumDTO.class);
        } catch (Exception e) {
            log.error("Error searching albums: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public List<AlbumDTO> getRecommendedAlbums(int limit) {
        try {
            String url = apiConfig.buildAlbumsUrl("/recommendations?limit=" + limit);
            return apiClient.getList(url, AlbumDTO.class);
        } catch (Exception e) {
            log.error("Error fetching recommended albums: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public AlbumDTO getAlbumContainsThisSong(Long songId) {
        try {
            String url = apiConfig.buildAlbumsUrl("/album-by-song?songId=" + songId);
            return apiClient.get(url, AlbumDTO.class);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<AlbumDTO> getAlbumsByArtistId(Long artistId) {
        try {
            String url = apiConfig.buildAlbumsUrl("/albums-by-artist?artistId=" + artistId);
            return apiClient.getList(url, AlbumDTO.class);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @Override
    public String getAlbumCoverId(Long albumId) {
        return "";
    }

    @Override
    public AlbumDTO createAlbum(AlbumRequestDTO albumRequestDTO) {
        try {
            String url = apiConfig.buildAlbumsUrl("/create");

            Map<String, Object> parts = new HashMap<>();
            parts.put("title", albumRequestDTO.getTitle());
            parts.put("artistId", albumRequestDTO.getArtistId());
            parts.put("releaseYear", albumRequestDTO.getReleaseYear());

            if (albumRequestDTO.getAlbumCover() != null) {
                parts.put("albumCover", albumRequestDTO.getAlbumCover());
            }

            AlbumDTO result = apiClient.postMultipart(url, parts, AlbumDTO.class);
            return result;
        } catch (Exception e) {
            log.error("Error creating song", e);
            return null;
        }
    }

    @Override
    public List<AlbumDTO> findAllAlbums() {
        try {
            String url = apiConfig.buildAlbumsUrl("/all");
            return apiClient.getList(url, AlbumDTO.class);
        } catch (Exception e) {
            log.error("Error fetching all albums: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}
