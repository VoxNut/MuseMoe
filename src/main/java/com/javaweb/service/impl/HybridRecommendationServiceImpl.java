package com.javaweb.service.impl;

import com.javaweb.model.dto.SongDTO;
import com.javaweb.service.RecommendationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
@Primary
public class HybridRecommendationServiceImpl implements RecommendationService {

    @Qualifier("recommendationServiceImpl")
    private RecommendationService contentBasedService;

    @Qualifier("collaborativeFilteringServiceImpl")
    private RecommendationService collaborativeService;

    private RecommendationServiceImpl recommendationServiceImpl;

    public HybridRecommendationServiceImpl(
            @Qualifier("recommendationServiceImpl") RecommendationService contentBasedService,
            @Qualifier("collaborativeFilteringServiceImpl") RecommendationService collaborativeService,
            RecommendationServiceImpl recommendationServiceImpl) {
        this.contentBasedService = contentBasedService;
        this.collaborativeService = collaborativeService;
        this.recommendationServiceImpl = recommendationServiceImpl;
    }

    @Override
    public List<SongDTO> getRecommendedSongs(Integer limit) {
        // Get recommendations from both services
        List<SongDTO> contentBasedRecommendations = contentBasedService.getRecommendedSongs(limit);
        List<SongDTO> collaborativeRecommendations = collaborativeService.getRecommendedSongs(limit);

        Map<Long, SongDTO> recommendationMap = getLongSongDTOMap(limit, contentBasedRecommendations, collaborativeRecommendations);

        // Convert to list
        List<SongDTO> recommendations = new ArrayList<>(recommendationMap.values());

        // Apply diversity to ensure varied recommendations
        if (recommendations.size() > limit) {
            return recommendationServiceImpl.ensureDiversity(recommendations, limit);
        }

        Collections.shuffle(recommendations);
        return recommendations.subList(0, Math.min(recommendations.size(), limit));
    }

    private static Map<Long, SongDTO> getLongSongDTOMap(Integer limit, List<SongDTO> contentBasedRecommendations, List<SongDTO> collaborativeRecommendations) {
        Map<Long, SongDTO> recommendationMap = new HashMap<>();

        // Add content-based recommendations (60% weight)
        int contentBasedCount = Math.min(contentBasedRecommendations.size(), (int) (limit * 0.6));
        for (int i = 0; i < contentBasedCount && i < contentBasedRecommendations.size(); i++) {
            SongDTO song = contentBasedRecommendations.get(i);
            recommendationMap.put(song.getId(), song);
        }

        // Add collaborative recommendations (40% weight)
        int collaborativeCount = Math.min(collaborativeRecommendations.size(), (int) (limit * 0.4));
        for (int i = 0; i < collaborativeCount && i < collaborativeRecommendations.size(); i++) {
            SongDTO song = collaborativeRecommendations.get(i);
            recommendationMap.put(song.getId(), song);
        }
        return recommendationMap;
    }
}