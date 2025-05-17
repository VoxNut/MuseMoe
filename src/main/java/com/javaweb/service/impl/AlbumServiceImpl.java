package com.javaweb.service.impl;

import com.javaweb.converter.AlbumConverter;
import com.javaweb.entity.AlbumEntity;
import com.javaweb.model.dto.AlbumDTO;
import com.javaweb.model.request.AlbumRequestDTO;
import com.javaweb.repository.AlbumRepository;
import com.javaweb.service.AlbumService;
import com.javaweb.service.ArtistService;
import com.javaweb.service.PlayHistoryService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlbumServiceImpl implements AlbumService {

    private final AlbumRepository albumRepository;
    private final AlbumConverter albumConverter;
    private final GoogleDriveService googleDriveService;
    private final SharedSearchService sharedSearchService;
    private final PlayHistoryService playHistoryService;
    private final ArtistService artistService;
    @PersistenceContext
    private EntityManager entityManager;


    @Override
    public AlbumDTO getAlbumContainsThisSong(Long songId) {
        return albumConverter.toDTO(albumRepository.findAlbumEntitiesBySongsIsIn(songId));
    }

    @Override
    public AlbumDTO createAlbum(AlbumRequestDTO albumRequestDTO) {
        try {
            if (albumRequestDTO.getAlbumCover() != null && !albumRequestDTO.getAlbumCover().isEmpty()) {
                String driveFileId = googleDriveService.uploadImageFile(
                        albumRequestDTO.getAlbumCover(),
                        GoogleDriveService.ALBUM_COVER_FOLDER_ID
                );
                albumRequestDTO.setGoogleDriveFileId(driveFileId);
                log.info("Uploaded album cover image to Google Drive with ID: {}", driveFileId);
            }
            AlbumDTO res = albumConverter.toDTO(albumRepository.save(albumConverter.toEntity(albumRequestDTO)));
            return res;
        } catch (Exception e) {
            log.error("Cannot create album: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public List<AlbumDTO> getAlbumsByArtistId(Long artistId) {
        try {
            return albumRepository.findAlbumEntitiesByArtist_Id(artistId)
                    .stream()
                    .map(albumConverter::toDTO)
                    .toList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @Override
    public List<AlbumDTO> getRecommendedAlbums(int limit) {
        try {

            // Step 1: Get user's listening history to find their favorite artists
            List<Long> recentlyPlayedSongIds = playHistoryService.fetchRecentPlayedSongIds(50);

            // Step 2: Find artists from these songs
            Set<Long> artistIds = new HashSet<>();
            for (Long songId : recentlyPlayedSongIds) {
                List<Long> songArtistIds = artistService.getArtistsIdBySongId(songId);
                artistIds.addAll(songArtistIds);
            }

            // Step 3: Find albums by these artists
            List<AlbumEntity> recommendedAlbums = new ArrayList<>();

            if (!artistIds.isEmpty()) {
                recommendedAlbums.addAll(albumRepository.findByArtistIdIn(new ArrayList<>(artistIds)));
            }

            // Step 4: If don't have enough albums, add some popular/trending ones
            if (recommendedAlbums.size() < limit) {
                // Find albums with most played songs
                List<AlbumEntity> trendingAlbums = findTrendingAlbums(limit - recommendedAlbums.size());

                // Add only albums that aren't already in the recommendations
                Set<Long> existingAlbumIds = recommendedAlbums.stream()
                        .map(AlbumEntity::getId)
                        .collect(Collectors.toSet());

                for (AlbumEntity album : trendingAlbums) {
                    if (!existingAlbumIds.contains(album.getId())) {
                        recommendedAlbums.add(album);
                    }
                }
            }

            // Step 5: Shuffle the results for variety and apply limit
            Collections.shuffle(recommendedAlbums);
            List<AlbumDTO> result = recommendedAlbums.stream()
                    .limit(limit)
                    .map(albumConverter::toDTO)
                    .collect(Collectors.toList());

            return result;
        } catch (Exception e) {
            log.error("Error getting recommended albums: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private List<AlbumEntity> findTrendingAlbums(int limit) {
        try {
            String query = "SELECT a.* FROM album a " +
                    "JOIN song s ON s.album_id = a.id " +
                    "LEFT JOIN play_history ph ON s.id = ph.song_id " +
                    "WHERE a.id IS NOT NULL " +
                    "GROUP BY a.id " +
                    "ORDER BY COUNT(ph.id) DESC " +
                    "LIMIT ?";

            Query nativeQuery = entityManager.createNativeQuery(query, AlbumEntity.class);
            nativeQuery.setParameter(1, limit);

            @SuppressWarnings("unchecked")
            List<AlbumEntity> results = nativeQuery.getResultList();

            return results;
        } catch (Exception e) {
            log.error("Error finding trending albums", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<AlbumDTO> searchAlbums(String query, int limit) {
        try {
            return sharedSearchService.findAlbumsByQuery(query, limit);
        } catch (Exception e) {
            log.error("Error searching albums: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public AlbumDTO fetchAlbumById(Long albumId) {
        return albumRepository.findById(albumId)
                .map(albumConverter::toDTO)
                .orElse(null);
    }
}
