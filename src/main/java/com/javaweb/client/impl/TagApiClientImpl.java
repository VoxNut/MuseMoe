package com.javaweb.client.impl;

import com.javaweb.client.ApiConfig;
import com.javaweb.client.client_service.TagApiClient;
import com.javaweb.model.dto.TagDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
public class TagApiClientImpl implements TagApiClient {
    private final ApiClient apiClient;
    private final UrlEncoder urlEncoder;
    private final ApiConfig apiConfig;

    @Override
    public Map<String, Integer> fetchTopTags(Integer limit) {
        try {
            String url = apiConfig.buildTagsUrl("/top_tags?limit=" + limit);
            return apiClient.get(url, Map.class);
        } catch (Exception e) {
            log.error("Error fetching top tags: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public List<TagDTO> fetchAllTags() {
        try {
            String url = apiConfig.buildTagsUrl("/all");
            return apiClient.getList(url, TagDTO.class);
        } catch (Exception e) {
            log.error("Error fetching all tags: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public List<TagDTO> findTagsBySongId(Long id) {
        try {
            String url = apiConfig.buildTagsUrl("/tags-by-song?songId=" + id);
            return apiClient.getList(url, TagDTO.class);
        } catch (Exception e) {
            log.error("Error fetching tags by song id: {}", e.getMessage(), e);
            return null;
        }
    }
}
