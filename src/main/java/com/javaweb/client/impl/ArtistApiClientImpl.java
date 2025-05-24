package com.javaweb.client.impl;

import com.javaweb.client.ApiConfig;
import com.javaweb.client.client_service.ArtistApiClient;
import com.javaweb.model.dto.ArtistDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
public class ArtistApiClientImpl implements ArtistApiClient {

    private final ApiClient apiClient;
    private final ApiConfig apiConfig;


    @Override
    public List<ArtistDTO> findArtistsBySongId(Long songId) {
        try {
            String url = apiConfig.buildArtistsUrl("/artists-by-song?songId=" + songId);
            return apiClient.getList(url, ArtistDTO.class);
        } catch (Exception e) {
            log.error("Error searching artists: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public ArtistDTO findArtistById(Long artistId) {
        try {
            String url = apiConfig.buildArtistsUrl("/artist?artistId=" + artistId);
            return apiClient.get(url, ArtistDTO.class);
        } catch (Exception e) {
            log.error("Error searching artist: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public boolean checkArtistFollowed(Long artistId) {
        try {
            String url = apiConfig.buildArtistsUrl("/artist_followed?artistId=" + artistId);
            return apiClient.get(url, Boolean.class);
        } catch (Exception e) {
            log.error("Error checking artist followed: {}", e.getMessage(), e);
            return false;
        }
    }

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

    @Override
    public boolean createArtist(String stageName, String bio, MultipartFile artistProfilePicture) {
        try {
            String url = apiConfig.buildArtistsUrl("/create");

            // Create a map of parts for multipart form data
            Map<String, Object> parts = new HashMap<>();
            parts.put("stageName", stageName);
            parts.put("bio", bio);

            if (artistProfilePicture != null) {
                parts.put("artistProfilePicture", artistProfilePicture);
            }


            Object result = apiClient.postMultipart(url, parts, Object.class);
            return result != null;
        } catch (Exception e) {
            log.error("Error creating artist: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public List<ArtistDTO> findAllArtists() {
        try {
            String url = apiConfig.buildArtistsUrl("/all");
            return apiClient.getList(url, ArtistDTO.class);
        } catch (Exception e) {
            log.error("Error fetching all artists: {}", e.getMessage(), e);
            return null;
        }
    }
}
