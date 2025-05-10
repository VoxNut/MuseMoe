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
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
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
    private final EntityManager entityManager;

    @Scheduled(cron = "0 0 3 * * ?")
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

            // 5. Enhance with content-based signals - Calculate content similarity
            for (Long likedSongId : userLikes) {
                // Get all available songs
                List<Long> availableSongIds = songRepository.findAllIds();
                availableSongIds.removeAll(userLikes); // Remove already liked songs

                for (Long candidateSongId : availableSongIds) {
                    double itemSimilarity = calculateItemSimilarity(likedSongId, candidateSongId);

                    // Only consider songs with reasonable similarity
                    if (itemSimilarity > 0.2) {
                        songRecommendationScores.merge(candidateSongId, itemSimilarity * 0.5, Double::sum);
                    }
                }
            }

            // 6. Save to database
            UserEntity user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                // Delete existing recommendations
                recommendationsRepository.deleteByUserId(userId);

                // Add new ones - normalize and sort by score
                List<Map.Entry<Long, Double>> sortedRecommendations = songRecommendationScores.entrySet()
                        .stream()
                        .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                        .limit(50) // Limit to top 50 recommendations
                        .collect(Collectors.toList());

                List<SongRecommendationsEntity> recommendations = new ArrayList<>();
                for (Map.Entry<Long, Double> scoreEntry : sortedRecommendations) {
                    Long songId = scoreEntry.getKey();
                    Double score = scoreEntry.getValue();

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

    // Add to CollaborativeFilteringServiceImpl.java
    private double calculateItemSimilarity(Long songId1, Long songId2) {
        try {
            // Get tags for both songs
            String query = "SELECT t.id, t.tag_type FROM tag t " +
                    "JOIN song_tags st ON t.id = st.tag_id " +
                    "WHERE st.song_id = ? OR st.song_id = ?";

            Query nativeQuery = entityManager.createNativeQuery(query);
            nativeQuery.setParameter(1, songId1);
            nativeQuery.setParameter(2, songId2);

            List<Object[]> results = nativeQuery.getResultList();

            // Group tags by song
            Map<Long, Set<Long>> songTags = new HashMap<>();
            songTags.put(songId1, new HashSet<>());
            songTags.put(songId2, new HashSet<>());

            for (Object[] row : results) {
                Long tagId = ((Number) row[0]).longValue();
                String songIdStr = (String) row[1];
                Long songId = Long.parseLong(songIdStr);

                songTags.get(songId).add(tagId);
            }

            // Calculate Jaccard similarity of tags
            Set<Long> tags1 = songTags.get(songId1);
            Set<Long> tags2 = songTags.get(songId2);

            Set<Long> intersection = new HashSet<>(tags1);
            intersection.retainAll(tags2);

            Set<Long> union = new HashSet<>(tags1);
            union.addAll(tags2);

            return union.isEmpty() ? 0 : (double) intersection.size() / union.size();
        } catch (Exception e) {
            log.error("Error calculating item similarity", e);
            return 0;
        }
    }

}
