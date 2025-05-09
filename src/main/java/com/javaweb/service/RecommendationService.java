package com.javaweb.service;

import com.javaweb.model.dto.SongDTO;

import java.util.List;

public interface RecommendationService {
    List<SongDTO> getRecommendedSongs(Integer limit);
}
