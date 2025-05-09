package com.javaweb.api;

import com.javaweb.model.dto.SongDTO;
import com.javaweb.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationAPI {

    private final RecommendationService recommendationService;

    @GetMapping
    public ResponseEntity<List<SongDTO>> getRecommendations(@RequestParam(name = "limit", defaultValue = "8") Integer limit) {
        List<SongDTO> recommendations = recommendationService.getRecommendedSongs(limit);
        return ResponseEntity.ok(recommendations);
    }
}