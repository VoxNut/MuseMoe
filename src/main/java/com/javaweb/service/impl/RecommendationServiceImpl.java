package com.javaweb.service.impl;

import com.javaweb.converter.SongConverter;
import com.javaweb.entity.SongEntity;
import com.javaweb.model.dto.SongDTO;
import com.javaweb.service.ArtistService;
import com.javaweb.service.PlayHistoryService;
import com.javaweb.service.RecommendationService;
import com.javaweb.service.SongLikesService;
import com.javaweb.utils.SecurityUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Primary
public class RecommendationServiceImpl implements RecommendationService {

    private final SongLikesService songLikesService;
    private final PlayHistoryService playHistoryService;
    private final ArtistService artistService;
    private final SongConverter songConverter;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public List<SongDTO> getRecommendedSongs(Integer limit) {
        // 1. Get user's liked songs
        List<Long> likedSongIds = songLikesService.getUserLikedSongsIds();

        // 2. Get user's play history
        List<Long> playHistorySongIds = playHistoryService.fetchRecentPlayedSongIds(20);


        // 3. Find artists from these songs
        Set<Long> relevantArtistIds = findArtistsFromSongs(likedSongIds, playHistorySongIds);

        // 4. Get similar songs based on artists
        List<SongDTO> artistBasedRecommendations = getSongsByArtists(relevantArtistIds,
                likedSongIds,
                playHistorySongIds,
                limit / 2);
        Long userId = SecurityUtils.getPrincipal().getId();

        // 5. Get trending songs (not listened by user)
        List<SongDTO> trendingSongs = getTrendingSongs(userId, likedSongIds,
                playHistorySongIds,
                limit - artistBasedRecommendations.size());

        List<SongDTO> recommendations = new ArrayList<>();
        recommendations.addAll(artistBasedRecommendations);
        recommendations.addAll(trendingSongs);
        Collections.shuffle(recommendations);

        return recommendations.subList(0, Math.min(recommendations.size(), limit));
    }

    public Set<Long> findArtistsFromSongs(List<Long> likedSongIds, List<Long> playHistorySongIds) {
        Set<Long> artistIds = new HashSet<>();
        likedSongIds.forEach(songId -> {
                    List<Long> currentArtistIds = artistService.getArtistsIdBySongId(songId);
                    artistIds.addAll(currentArtistIds);
                }
        );

        playHistorySongIds.forEach(songId -> {
                    List<Long> currentArtistIds = artistService.getArtistsIdBySongId(songId);
                    artistIds.addAll(currentArtistIds);
                }
        );

        return artistIds;
    }

    public List<SongDTO> getSongsByArtists(Set<Long> artistIds,
                                           List<Long> excludeSongIds1,
                                           List<Long> excludeSongIds2,
                                           int limit) {
        if (artistIds.isEmpty()) {
            return new ArrayList<>();
        }

        try {
            Set<Long> excludeIds = new HashSet<>();
            excludeIds.addAll(excludeSongIds1);
            excludeIds.addAll(excludeSongIds2);

            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("SELECT DISTINCT s.* FROM song s ");
            queryBuilder.append("JOIN song_artist a_s ON s.id = a_s.song_id ");
            queryBuilder.append("WHERE a_s.artist_id IN (");

            String artistIdPlaceholders = artistIds.stream()
                    .map(id -> "?")
                    .collect(Collectors.joining(","));
            queryBuilder.append(artistIdPlaceholders);
            queryBuilder.append(") ");

            if (!excludeIds.isEmpty()) {
                queryBuilder.append("AND s.id NOT IN (");
                String excludeIdPlaceholders = excludeIds.stream()
                        .map(id -> "?")
                        .collect(Collectors.joining(","));
                queryBuilder.append(excludeIdPlaceholders);
                queryBuilder.append(") ");
            }

            // Randomize results to provide variety and limit
            queryBuilder.append("ORDER BY RAND() LIMIT ?");

            Query query = entityManager.createNativeQuery(queryBuilder.toString(), SongEntity.class);

            int paramIndex = 1;
            for (Long artistId : artistIds) {
                query.setParameter(paramIndex++, artistId);
            }

            if (!excludeIds.isEmpty()) {
                for (Long excludeId : excludeIds) {
                    query.setParameter(paramIndex++, excludeId);
                }
            }

            query.setParameter(paramIndex, limit);

            // Execute query and convert results
            @SuppressWarnings("unchecked")
            List<SongEntity> songs = query.getResultList();

            return songs.stream()
                    .map(songConverter::toDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting song by artists", e);
            return new ArrayList<>();
        }
    }

    public List<SongDTO> getTrendingSongs(Long userId,
                                          List<Long> excludeSongIds1,
                                          List<Long> excludeSongIds2,
                                          int limit) {
        if (limit <= 0) {
            return new ArrayList<>();
        }

        try {
            Set<Long> excludeIds = new HashSet<>();
            excludeIds.addAll(excludeSongIds1);
            excludeIds.addAll(excludeSongIds2);

            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("SELECT s.* FROM song s ");
            queryBuilder.append("LEFT JOIN play_history ph ON s.id = ph.song_id ");

            queryBuilder.append("WHERE 1=1 ");

            if (!excludeIds.isEmpty()) {
                queryBuilder.append("AND s.id NOT IN (");
                String excludeIdPlaceholders = excludeIds.stream()
                        .map(id -> "?")
                        .collect(Collectors.joining(","));
                queryBuilder.append(excludeIdPlaceholders);
                queryBuilder.append(") ");
            }

            queryBuilder.append("GROUP BY s.id ");

            queryBuilder.append("ORDER BY COUNT(ph.id) DESC, s.created_at DESC ");
            queryBuilder.append("LIMIT ?");

            Query query = entityManager.createNativeQuery(queryBuilder.toString(), SongEntity.class);

            int paramIndex = 1;
            if (!excludeIds.isEmpty()) {
                for (Long excludeId : excludeIds) {
                    query.setParameter(paramIndex++, excludeId);
                }
            }
            query.setParameter(paramIndex, limit);

            @SuppressWarnings("unchecked")
            List<SongEntity> songs = query.getResultList();

            return songs.stream()
                    .map(songConverter::toDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting trending songs", e);
            return new ArrayList<>();
        }
    }
}