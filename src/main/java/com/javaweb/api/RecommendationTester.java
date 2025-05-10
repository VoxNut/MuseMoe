package com.javaweb.api;

import com.javaweb.model.dto.SongDTO;
import com.javaweb.model.dto.TagDTO;
import com.javaweb.service.RecommendationService;
import com.javaweb.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@Controller
@RequestMapping("/api/recommendation_tester")
public class RecommendationTester {
    
    private final RecommendationService recommendationService;
    private final TagService tagService;

    @GetMapping("/user/{userId}")
    public void testUserRecommendations(@PathVariable Long userId) {
        List<SongDTO> recommendations = recommendationService.getRecommendedSongs(userId, 10);
        for (SongDTO song : recommendations) {
            List<String> tags = tagService.findTagsBySongId(song.getId())
                    .stream()
                    .map(TagDTO::getName)
                    .collect(Collectors.toList());
            System.out.println("Song: " + song.getTitle() + ", Tags: " + String.join(", ", tags));
        }
    }
}