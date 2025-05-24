package com.javaweb.client.client_service;

import com.javaweb.model.dto.TagDTO;

import java.util.List;
import java.util.Map;

public interface TagApiClient {

    Map<String, Integer> fetchTopTags(Integer limit);

    List<TagDTO> fetchAllTags();

    List<TagDTO> findTagsBySongId(Long id);
}
