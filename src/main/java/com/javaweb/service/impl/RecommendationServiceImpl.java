package com.javaweb.service.impl;

import com.javaweb.converter.SongConverter;
import com.javaweb.entity.SongEntity;
import com.javaweb.entity.TagEntity;
import com.javaweb.model.dto.SongDTO;
import com.javaweb.service.ArtistService;
import com.javaweb.service.PlayHistoryService;
import com.javaweb.service.RecommendationService;
import com.javaweb.service.SongLikesService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
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

        // Combined exclusion list
        List<Long> excludeSongIds = new ArrayList<>();
        excludeSongIds.addAll(likedSongIds);
        excludeSongIds.addAll(playHistorySongIds);

        // 3. Get recommendations from different sources
        Set<Long> relevantArtistIds = findArtistsFromSongs(likedSongIds, playHistorySongIds);

        // Divide the limit between different recommendation approaches
        int artistBasedLimit = limit / 3;
        int tagBasedLimit = limit / 3;
        int trendingLimit = limit - artistBasedLimit - tagBasedLimit;

        // 4. Get artist-based recommendations
        List<SongDTO> artistBasedRecommendations = getSongsByArtists(
                relevantArtistIds, likedSongIds, playHistorySongIds, artistBasedLimit);

        // 5. Get tag-based recommendations
        List<SongDTO> tagBasedRecommendations = getTagBasedRecommendations(
                likedSongIds, excludeSongIds, tagBasedLimit);

        // 6. Get trending songs
        List<SongDTO> trendingSongs = getTrendingSongs(
                likedSongIds, playHistorySongIds, trendingLimit);

        // 7. Combine all recommendations
        List<SongDTO> recommendations = new ArrayList<>();
        recommendations.addAll(artistBasedRecommendations);
        recommendations.addAll(tagBasedRecommendations);
        recommendations.addAll(trendingSongs);

        recommendations = ensureDiversity(recommendations, limit);

        if (recommendations.size() < limit) {
            Collections.shuffle(recommendations);
            return recommendations.subList(0, Math.min(recommendations.size(), limit));
        }

        return recommendations;
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

    public List<SongDTO> getTrendingSongs(
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

    public List<SongDTO> getTagBasedRecommendations(List<Long> likedSongIds, List<Long> excludeSongIds, int limit) {
        try {
            if (likedSongIds.isEmpty()) {
                return new ArrayList<>();
            }

            StringBuilder tagQueryBuilder = new StringBuilder();
            tagQueryBuilder.append("SELECT t.id, COUNT(t.id) as tag_count FROM tag t ");
            tagQueryBuilder.append("JOIN song_tags st ON t.id = st.tag_id ");
            tagQueryBuilder.append("WHERE st.song_id IN (");
            tagQueryBuilder.append(likedSongIds.stream().map(id -> "?").collect(Collectors.joining(",")));
            tagQueryBuilder.append(") GROUP BY t.id ORDER BY tag_count DESC LIMIT 10");

            Query tagQuery = entityManager.createNativeQuery(tagQueryBuilder.toString());

            int paramIndex = 1;
            for (Long songId : likedSongIds) {
                tagQuery.setParameter(paramIndex++, songId);
            }

            List<Object[]> tagResults = tagQuery.getResultList();
            List<Long> topTagIds = tagResults.stream()
                    .map(row -> ((Number) row[0]).longValue())
                    .collect(Collectors.toList());

            if (topTagIds.isEmpty()) {
                return new ArrayList<>();
            }

            // Find songs with these tags that user hasn't heard yet
            StringBuilder songQueryBuilder = new StringBuilder();
            songQueryBuilder.append("SELECT DISTINCT s.* FROM song s ");
            songQueryBuilder.append("JOIN song_tags st ON s.id = st.song_id ");
            songQueryBuilder.append("WHERE st.tag_id IN (");
            songQueryBuilder.append(topTagIds.stream().map(id -> "?").collect(Collectors.joining(",")));
            songQueryBuilder.append(") ");

            // Exclude songs user has already interacted with
            Set<Long> excludeIds = new HashSet<>(excludeSongIds);
            if (!excludeIds.isEmpty()) {
                songQueryBuilder.append("AND s.id NOT IN (");
                songQueryBuilder.append(excludeIds.stream().map(id -> "?").collect(Collectors.joining(",")));
                songQueryBuilder.append(") ");
            }

            songQueryBuilder.append("ORDER BY RAND() LIMIT ?");

            Query songQuery = entityManager.createNativeQuery(songQueryBuilder.toString(), SongEntity.class);

            paramIndex = 1;
            for (Long tagId : topTagIds) {
                songQuery.setParameter(paramIndex++, tagId);
            }

            for (Long excludeId : excludeIds) {
                songQuery.setParameter(paramIndex++, excludeId);
            }

            songQuery.setParameter(paramIndex, limit);

            @SuppressWarnings("unchecked")
            List<SongEntity> songs = songQuery.getResultList();

            return songs.stream()
                    .map(songConverter::toDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting tag-based recommendations", e);
            return new ArrayList<>();
        }
    }

    public List<SongDTO> ensureDiversity(List<SongDTO> recommendations, int limit) {
        // Get song IDs
        List<Long> songIds = recommendations.stream()
                .map(SongDTO::getId)
                .collect(Collectors.toList());

        if (songIds.isEmpty()) {
            return new ArrayList<>();
        }

        // Get tags for these songs
        Map<Long, Set<TagEntity.TagType>> songTagTypes = new HashMap<>();

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT st.song_id, t.tag_type FROM song_tags st ");
        queryBuilder.append("JOIN tag t ON st.tag_id = t.id ");
        queryBuilder.append("WHERE st.song_id IN (");
        queryBuilder.append(songIds.stream().map(id -> "?").collect(Collectors.joining(",")));
        queryBuilder.append(")");

        Query query = entityManager.createNativeQuery(queryBuilder.toString());

        int paramIndex = 1;
        for (Long songId : songIds) {
            query.setParameter(paramIndex++, songId);
        }

        List<Object[]> results = query.getResultList();

        // Map song IDs to their tag types
        for (Object[] row : results) {
            Long songId = ((Number) row[0]).longValue();
            String tagTypeStr = (String) row[1];
            TagEntity.TagType tagType = TagEntity.TagType.valueOf(tagTypeStr);

            songTagTypes.computeIfAbsent(songId, k -> new HashSet<>()).add(tagType);
        }

        // Diversify by selecting songs with different tag types
        List<SongDTO> diversifiedRecommendations = new ArrayList<>();
        Map<TagEntity.TagType, Integer> tagTypeCounts = new EnumMap<>(TagEntity.TagType.class);

        for (TagEntity.TagType type : TagEntity.TagType.values()) {
            tagTypeCounts.put(type, 0);
        }

        for (SongDTO song : recommendations) {
            Set<TagEntity.TagType> songTypes = songTagTypes.getOrDefault(song.getId(), new HashSet<>());

            if (songTypes.isEmpty() || diversifiedRecommendations.size() < limit / 2) {
                diversifiedRecommendations.add(song);
            } else {
                boolean isUnderrepresentedType = songTypes.stream()
                        .anyMatch(type -> tagTypeCounts.get(type) < limit / TagEntity.TagType.values().length);

                if (isUnderrepresentedType) {
                    diversifiedRecommendations.add(song);
                    songTypes.forEach(type -> tagTypeCounts.put(type, tagTypeCounts.get(type) + 1));
                }
            }

            if (diversifiedRecommendations.size() >= limit) {
                break;
            }
        }
        return diversifiedRecommendations;
    }


}