package com.javaweb.service.impl;


import com.javaweb.converter.SongConverter;
import com.javaweb.entity.SongEntity;
import com.javaweb.entity.SongRecommendationsEntity;
import com.javaweb.entity.UserEntity;
import com.javaweb.model.dto.SongDTO;
import com.javaweb.repository.SongLikesRepository;
import com.javaweb.repository.SongRecommendationsRepository;
import com.javaweb.repository.SongRepository;
import com.javaweb.repository.UserRepository;
import com.javaweb.service.RecommendationService;
import com.javaweb.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CollaborativeFilteringServiceImpl implements RecommendationService {

    private final SongLikesRepository songLikesRepository;
    private final UserRepository userRepository;
    private final SongRepository songRepository;
    private final SongRecommendationsRepository recommendationsRepository;
    private final SongConverter songConverter;

    // This would run periodically to update recommendations
    @Scheduled(cron = "0 0 3 * * ?") // Run at 3 AM every day
    public void updateRecommendations() {
        log.info("Starting recommendation calculations");
        Map<Long, Set<Long>> userSongLikes = new HashMap<>();

        // 1. Get all users' liked songs
        songLikesRepository.findAll().forEach(like -> {
            userSongLikes.computeIfAbsent(like.getUser().getId(), k -> new HashSet<>())
                    .add(like.getSong().getId());
        });

        // 2. For each user
        for (Long userId : userSongLikes.keySet()) {
            Set<Long> userLikes = userSongLikes.get(userId);

            // 3. Calculate similarity with other users
            Map<Long, Double> userSimilarities = new HashMap<>();

            for (Long otherUserId : userSongLikes.keySet()) {
                if (otherUserId.equals(userId)) continue;

                Set<Long> otherUserLikes = userSongLikes.get(otherUserId);

                // Calculate Jaccard similarity coefficient
                Set<Long> intersection = new HashSet<>(userLikes);
                intersection.retainAll(otherUserLikes);

                Set<Long> union = new HashSet<>(userLikes);
                union.addAll(otherUserLikes);

                if (!union.isEmpty()) {
                    double similarity = (double) intersection.size() / union.size();
                    userSimilarities.put(otherUserId, similarity);
                }
            }

            // 4. Get potential recommendations from similar users
            Map<Long, Double> songRecommendationScores = new HashMap<>();

            for (Map.Entry<Long, Double> entry : userSimilarities.entrySet()) {
                Long otherUserId = entry.getKey();
                Double similarity = entry.getValue();

                if (similarity > 0.1) { // Only consider somewhat similar users
                    Set<Long> otherUserLikes = userSongLikes.get(otherUserId);

                    for (Long songId : otherUserLikes) {
                        if (!userLikes.contains(songId)) {
                            // Weight recommendation by similarity
                            songRecommendationScores.merge(songId, similarity, Double::sum);
                        }
                    }
                }
            }

            // 5. Save to database
            UserEntity user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                // Delete existing recommendations
                recommendationsRepository.deleteByUserId(userId);

                // Add new ones
                List<SongRecommendationsEntity> recommendations = new ArrayList<>();
                for (Map.Entry<Long, Double> entry : songRecommendationScores.entrySet()) {
                    Long songId = entry.getKey();
                    Double score = entry.getValue();

                    SongEntity song = songRepository.findById(songId).orElse(null);
                    if (song != null) {
                        SongRecommendationsEntity rec = new SongRecommendationsEntity();
                        rec.setUser(user);
                        rec.setSong(song);
                        rec.setRecommendationStrength(score);
                        recommendations.add(rec);
                    }
                }

                recommendationsRepository.saveAll(recommendations);
            }
        }
        log.info("Finished recommendation calculations");
    }


    @Override
    public List<SongDTO> getRecommendedSongs(Integer limit) {
        Long userId = SecurityUtils.getPrincipal().getId();
        List<SongRecommendationsEntity> recommendations =
                recommendationsRepository.findByUserIdOrderByRecommendationStrengthDesc(userId);

        return recommendations.stream()
                .map(SongRecommendationsEntity::getSong)
                .map(songConverter::toDTO)
                .limit(limit)
                .collect(Collectors.toList());
    }

}
