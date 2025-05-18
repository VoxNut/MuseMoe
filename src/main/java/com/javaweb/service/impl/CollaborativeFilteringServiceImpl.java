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
import jakarta.transaction.Transactional;
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


    @Override
    public List<SongDTO> getRecommendedSongs(Long userId, Integer limit) {
        List<SongRecommendationsEntity> recommendations =
                recommendationsRepository.findByUserIdOrderByRecommendationStrengthDesc(userId);
        List<SongDTO> songDTOS = recommendations.stream()
                .map(SongRecommendationsEntity::getSong)
                .map(songConverter::toDTO)
                .limit(limit)
                .collect(Collectors.toList());
        return songDTOS;
    }

    @Scheduled(cron = "0 * */4 * * ?")
//    @Scheduled(cron = "0 */5 * * * * ")
    @Transactional
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

            // 5. Enhance with content-based signals, but only for songs from similar users
            Set<Long> candidateSongsFromSimilarUsers = new HashSet<>();

            // Collect songs from users with similarity > threshold
            for (Map.Entry<Long, Double> entry : userSimilarities.entrySet()) {
                Long otherUserId = entry.getKey();
                Double similarity = entry.getValue();

                if (similarity > 0.1) {
                    Set<Long> otherUserLikes = userSongLikes.get(otherUserId);
                    candidateSongsFromSimilarUsers.addAll(otherUserLikes);
                }
            }

            candidateSongsFromSimilarUsers.removeAll(userLikes);

            if (candidateSongsFromSimilarUsers.size() < 50) {
                List<Long> popularSongIds = findPopularSongIds(50 - candidateSongsFromSimilarUsers.size());
                candidateSongsFromSimilarUsers.addAll(popularSongIds);
            }

            for (Long likedSongId : userLikes) {
                for (Long candidateSongId : candidateSongsFromSimilarUsers) {
                    double itemSimilarity = calculateItemSimilarity(likedSongId, candidateSongId);

                    if (itemSimilarity > 0.2) {
                        songRecommendationScores.merge(candidateSongId, itemSimilarity * 0.5, Double::sum);
                    }
                }
            }

            // 6. Save to database
            UserEntity user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                recommendationsRepository.deleteByUserId(userId);

                List<Map.Entry<Long, Double>> sortedRecommendations = songRecommendationScores.entrySet()
                        .stream()
                        .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                        .limit(50)
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


    private List<Long> findPopularSongIds(int limit) {
        try {
            String query = "SELECT s.id FROM song s " +
                    "LEFT JOIN play_history ph ON s.id = ph.song_id " +
                    "GROUP BY s.id " +
                    "ORDER BY COUNT(ph.id) DESC " +
                    "LIMIT ?";

            Query nativeQuery = entityManager.createNativeQuery(query);
            nativeQuery.setParameter(1, limit);

            @SuppressWarnings("unchecked")
            List<Number> results = nativeQuery.getResultList();

            return results.stream()
                    .map(Number::longValue)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error finding popular songs", e);
            return new ArrayList<>();
        }
    }


    @Override
    public List<SongDTO> getRecommendedSongs(Integer limit) {
        Long userId = SecurityUtils.getPrincipal().getId();
        List<SongRecommendationsEntity> recommendations =
                recommendationsRepository.findByUserIdOrderByRecommendationStrengthDesc(userId);

        List<SongDTO> songDTOS = recommendations.stream()
                .map(SongRecommendationsEntity::getSong)
                .map(songConverter::toDTO)
                .limit(limit)
                .collect(Collectors.toList());
        return songDTOS;
    }

    private double calculateItemSimilarity(Long songId1, Long songId2) {
        if (songId1 == null || songId2 == null) {
            log.warn("Null song ID passed to calculateItemSimilarity");
            return 0;
        }

        if (songId1.equals(songId2)) {
            return 1.0;
        }
        try {
            String query = "SELECT st.song_id, t.id FROM tag t " +
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
                Long songId = ((Number) row[0]).longValue();
                Long tagId = ((Number) row[1]).longValue();

                if (songId.equals(songId1) || songId.equals(songId2)) {
                    songTags.get(songId).add(tagId);
                }
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
            log.error("Error calculating item similarity: {}", e.getMessage(), e);
            return 0;
        }
    }
}
