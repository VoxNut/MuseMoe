package com.javaweb.service.impl;

import com.ibm.icu.text.Transliterator;
import com.javaweb.converter.AlbumConverter;
import com.javaweb.converter.ArtistConverter;
import com.javaweb.converter.SongConverter;
import com.javaweb.entity.AlbumEntity;
import com.javaweb.entity.ArtistEntity;
import com.javaweb.entity.SongEntity;
import com.javaweb.model.dto.AlbumDTO;
import com.javaweb.model.dto.ArtistDTO;
import com.javaweb.model.dto.SongDTO;
import com.javaweb.repository.AlbumRepository;
import com.javaweb.repository.ArtistRepository;
import com.javaweb.repository.SongRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SharedSearchService {
    private final SongRepository songRepository;
    private final AlbumRepository albumRepository;
    private final ArtistRepository artistRepository;

    private final SongConverter songConverter;
    private final AlbumConverter albumConverter;
    private final ArtistConverter artistConverter;

    @PersistenceContext
    private EntityManager entityManager;

    private final Transliterator latinToKana = Transliterator.getInstance("Latin-Hiragana");
    private final Transliterator kanaToLatin = Transliterator.getInstance("Hiragana-Latin");

    /**
     * Search for songs based on query text
     */
    public List<SongDTO> findSongsByQuery(String query, int limit) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }

        // Normalize the query and create variants for better matching
        String normalizedQuery = normalizeText(query);
        Set<String> queryVariants = generateQueryVariants(normalizedQuery);

        // Combine results from all query variants with deduplication
        Set<SongEntity> results = new LinkedHashSet<>();

        // Search by exact title match first (higher priority)
        for (String variant : queryVariants) {
            List<SongEntity> exactMatches = songRepository.findByTitleContainingIgnoreCase(variant);
            results.addAll(exactMatches);

            if (results.size() >= limit) {
                break;
            }
        }

        // If we don't have enough results, perform a more comprehensive search
        if (results.size() < limit) {
            List<SongEntity> fullTextResults = performFullTextSongSearch(queryVariants, limit - results.size());
            results.addAll(fullTextResults);
        }

        // Convert to DTOs and apply final limit
        return results.stream()
                .limit(limit)
                .map(songConverter::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Search for albums based on query text and related entities
     */
    public List<AlbumDTO> findAlbumsByQuery(String query, int limit) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }

        String normalizedQuery = query.toLowerCase().trim();
        Set<AlbumEntity> results = new LinkedHashSet<>();

        // 1. Direct title matches (highest priority)
        List<AlbumEntity> directMatches = albumRepository.findByTitleContainingIgnoreCase(normalizedQuery);
        results.addAll(directMatches);

        // 2. Search for albums with songs matching the query
        if (results.size() < limit) {
            List<SongDTO> matchingSongs = findSongsByQuery(normalizedQuery, 50);
            if (!matchingSongs.isEmpty()) {
                Set<Long> albumIds = matchingSongs.stream()
                        .map(SongDTO::getAlbumId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());

                if (!albumIds.isEmpty()) {
                    List<AlbumEntity> albumsWithMatchingSongs = albumRepository.findAllById(albumIds);
                    results.addAll(albumsWithMatchingSongs);
                }
            }
        }

        // 3. Search for albums by artist name, but without using ArtistService
        if (results.size() < limit) {
            List<ArtistEntity> matchingArtists = artistRepository
                    .findByStageNameContainingIgnoreCaseOrderByFollowersCountDesc(normalizedQuery);

            if (!matchingArtists.isEmpty()) {
                Set<Long> artistIds = matchingArtists.stream()
                        .map(ArtistEntity::getId)
                        .collect(Collectors.toSet());

                if (!artistIds.isEmpty()) {
                    List<AlbumEntity> albumsByArtists = albumRepository.findByArtistIdIn(new ArrayList<>(artistIds));
                    results.addAll(albumsByArtists);
                }
            }
        }

        // Convert to DTOs and apply limit
        return results.stream()
                .limit(limit)
                .map(albumConverter::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Search for artists based on query text and related entities
     */
    public List<ArtistDTO> findArtistsByQuery(String query, int limit) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }

        String normalizedQuery = query.toLowerCase().trim();
        Set<ArtistEntity> results = new LinkedHashSet<>();

        // 1. Direct name matches (highest priority)
        List<ArtistEntity> directMatches = artistRepository
                .findByStageNameContainingIgnoreCaseOrderByFollowersCountDesc(normalizedQuery);
        results.addAll(directMatches);

        // 2. Find artists with songs matching the query
        if (results.size() < limit) {
            List<SongDTO> matchingSongs = findSongsByQuery(normalizedQuery, 50);
            if (!matchingSongs.isEmpty()) {
                for (SongDTO song : matchingSongs) {
                    if (song.getArtistIds() != null && !song.getArtistIds().isEmpty()) {
                        List<ArtistEntity> songArtists = artistRepository.findAllById(song.getArtistIds());
                        results.addAll(songArtists);
                    }
                }
            }
        }

        // 3. Find artists with albums matching the query, but without using AlbumService
        if (results.size() < limit) {
            List<AlbumEntity> matchingAlbums = albumRepository.findByTitleContainingIgnoreCase(normalizedQuery);
            if (!matchingAlbums.isEmpty()) {
                for (AlbumEntity album : matchingAlbums) {
                    if (album.getArtist() != null) {
                        results.add(album.getArtist());
                    }
                }
            }
        }

        // Convert to DTOs and apply limit
        return results.stream()
                .limit(limit)
                .map(artistConverter::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Helper method to normalize text for searching
     */
    private String normalizeText(String text) {
        if (text == null) return "";

        // Remove diacritical marks and convert to lowercase
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase();

        // Remove special characters except spaces and alphanumeric
        return normalized.replaceAll("[^a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    /**
     * Generate query variants for more comprehensive searching
     */
    private Set<String> generateQueryVariants(String query) {
        Set<String> variants = new HashSet<>();
        variants.add(query);

        // Add romaji/kana variants if Japanese characters are detected
        if (containsJapanese(query)) {
            variants.add(kanaToLatin.transliterate(query));
        } else {
            variants.add(latinToKana.transliterate(query));
        }

        // Add variants with different word separators
        variants.add(query.replace(" ", ""));  // No spaces

        // Add word-by-word variants for partial matching
        String[] words = query.split("\\s+");
        if (words.length > 1) {
            for (String word : words) {
                if (word.length() > 2) {  // Only add meaningful words
                    variants.add(word);
                }
            }
        }

        return variants;
    }

    /**
     * Check if text contains Japanese characters
     */
    private boolean containsJapanese(String text) {
        if (text == null) return false;

        for (char c : text.toCharArray()) {
            if (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.HIRAGANA ||
                    Character.UnicodeBlock.of(c) == Character.UnicodeBlock.KATAKANA ||
                    Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS) {
                return true;
            }
        }
        return false;
    }

    /**
     * Perform a full text search for songs using multiple query variants
     */
    private List<SongEntity> performFullTextSongSearch(Set<String> queryVariants, int limit) {
        StringBuilder queryBuilder = new StringBuilder();

        queryBuilder.append("SELECT DISTINCT s FROM SongEntity s ");
        queryBuilder.append("LEFT JOIN s.artists a ");
        queryBuilder.append("LEFT JOIN s.album alb ");
        queryBuilder.append("WHERE ");

        List<String> conditions = new ArrayList<>();
        for (int i = 0; i < queryVariants.size(); i++) {
            conditions.add("(LOWER(s.title) LIKE :query" + i + " OR " +
                    "LOWER(a.stageName) LIKE :query" + i + " OR " +
                    "LOWER(alb.title) LIKE :query" + i + ")");
        }

        queryBuilder.append(String.join(" OR ", conditions));

        queryBuilder.append(" ORDER BY CASE WHEN LOWER(s.title) = :exactTitle THEN 0 " +
                "WHEN LOWER(s.title) LIKE :startsWithTitle THEN 1 " +
                "WHEN EXISTS (SELECT 1 FROM s.artists a1 WHERE LOWER(a1.stageName) = :exactArtist) THEN 2 " +
                "ELSE 3 END, s.playCount DESC");

        try {
            Query query = entityManager.createQuery(queryBuilder.toString());

            int i = 0;
            String exactTitle = null;
            String exactArtist = null;

            for (String variant : queryVariants) {
                query.setParameter("query" + i, "%" + variant + "%");
                if (i == 0) {
                    exactTitle = variant.toLowerCase();
                    exactArtist = variant.toLowerCase();
                }
                i++;
            }

            query.setParameter("exactTitle", exactTitle);
            query.setParameter("startsWithTitle", exactTitle + "%");
            query.setParameter("exactArtist", exactArtist);

            query.setMaxResults(limit);

            @SuppressWarnings("unchecked")
            List<SongEntity> results = query.getResultList();
            return results;
        } catch (Exception e) {
            log.error("Error performing full text search: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}