package com.javaweb.service.impl;

import com.javaweb.converter.PlaylistConverter;
import com.javaweb.entity.*;
import com.javaweb.exception.EntityNotFoundException;
import com.javaweb.model.dto.PlaylistDTO;
import com.javaweb.repository.PlaylistRepository;
import com.javaweb.repository.SongRepository;
import com.javaweb.repository.UserRepository;
import com.javaweb.service.PlaylistService;
import com.javaweb.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PlaylistServiceImpl implements PlaylistService {

    private final PlaylistRepository playlistRepository;

    private final PlaylistConverter playlistConverter;

    private final UserRepository userRepository;
    private final SongRepository songRepository;

    @Override
    public List<PlaylistDTO> findPlaylistsByUserId() {
        try {
            List<PlaylistDTO> res = playlistRepository
                    .findPlaylistsByUserIdWithSongsOrdered(Objects.requireNonNull(SecurityUtils.getPrincipal()).getId())
                    .stream()
                    .map(playlistConverter::toDTO)
                    .collect(Collectors.toList());
            log.info("Successfully creating playlist list for user with ID: '{}'", Objects.requireNonNull(SecurityUtils.getPrincipal()).getId());
            return res;
        } catch (Exception e) {
            log.error("Failed to create playlist list for user with ID: '{}'", Objects.requireNonNull(SecurityUtils.getPrincipal()).getId(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public boolean createPlaylist(PlaylistDTO playlistDTO) {
        return false;
    }

    @Override
    public boolean updatePlaylist(PlaylistDTO playlistDTO) {
        return false;
    }

    @Override
    public boolean deletePlaylist(Long playlistId) {
        return false;
    }

    @Override
    public PlaylistDTO createPlaylist(String name, Long songId) {
        try {
            // Get the current user
            Long userId = Objects.requireNonNull(SecurityUtils.getPrincipal()).getId();
            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));

            // Create the playlist entity
            PlaylistEntity playlist = new PlaylistEntity();
            playlist.setName(name);
            playlist.setUser(user);
            playlist.setVisibility(PlaylistEntity.Visibility.PRIVATE);
            playlist.setLastUpdated(new Date());
            playlist.setPlaylistSongEntities(new ArrayList<>());

            // Save the playlist first to get its ID
            PlaylistEntity savedPlaylist = playlistRepository.save(playlist);

            // If a song was provided, add it to the playlist
            if (songId != null) {
                SongEntity song = songRepository.findById(songId)
                        .orElseThrow(() -> new EntityNotFoundException("Song not found with ID: " + songId));

                PlaylistSongEntity playlistSong = new PlaylistSongEntity();
                playlistSong.setPlaylist(savedPlaylist);
                playlistSong.setSong(song);
                playlistSong.setId(new PlaylistSongId(savedPlaylist.getId(), songId));
                playlistSong.setAddedAt(LocalDateTime.now());
                playlistSong.setPosition(0); // First song in the playlist

                // Add the song to the playlist's songs collection
                savedPlaylist.getPlaylistSongEntities().add(playlistSong);

                // Save the updated playlist
                savedPlaylist = playlistRepository.save(savedPlaylist);
            }

            // Convert to DTO and return
            PlaylistDTO playlistDTO = playlistConverter.toDTO(savedPlaylist);
            log.info("Successfully created playlist '{}' for user ID: {}", name, userId);
            return playlistDTO;

        } catch (Exception e) {
            log.error("Error creating playlist '{}': {}", name, e.getMessage(), e);
            throw e; // Rethrow to allow proper error handling in controller
        }
    }

    @Override
    public boolean addSongToPlaylist(Long playlistId, Long songId) {
        try {
            Long userId = Objects.requireNonNull(SecurityUtils.getPrincipal()).getId();

            PlaylistEntity playlist = playlistRepository.findById(playlistId)
                    .orElseThrow(() -> new EntityNotFoundException("Playlist not found with ID: " + playlistId));

            if (!playlist.getUser().getId().equals(userId)) {
                log.warn("User {} attempted to modify playlist {} owned by user {}", userId, playlistId, playlist.getUser().getId());
                return false;
            }

            SongEntity song = songRepository.findById(songId)
                    .orElseThrow(() -> new EntityNotFoundException("Song not found with ID: " + songId));

            boolean songExists = playlist.getPlaylistSongEntities().stream()
                    .anyMatch(ps -> ps.getSong().getId().equals(songId));

            if (songExists) {
                log.info("Song {} is already in playlist {}", songId, playlistId);
                return true;
            }

            PlaylistSongEntity playlistSong = new PlaylistSongEntity();
            playlistSong.setPlaylist(playlist);
            playlistSong.setSong(song);
            playlistSong.setId(new PlaylistSongId(playlistId, songId));
            playlistSong.setAddedAt(LocalDateTime.now());

            int nextPosition = playlist.getPlaylistSongEntities().size();
            playlistSong.setPosition(nextPosition + 1);

            playlist.getPlaylistSongEntities().add(playlistSong);

            playlist.setLastUpdated(new Date());

            playlistRepository.save(playlist);

            log.info("Added song {} to playlist {}", songId, playlistId);
            return true;

        } catch (Exception e) {
            log.error("Error adding song {} to playlist {}: {}", songId, playlistId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public List<PlaylistDTO> findAllPlaylists() {
        try {
            List<PlaylistDTO> res = playlistRepository.findAll()
                    .stream()
                    .map(playlistConverter::toDTO)
                    .collect(Collectors.toList());
            return res;
        } catch (Exception e) {
            return Collections.emptyList();
        }

    }

    @Override
    public List<PlaylistDTO> searchPlaylists(String query, int limit) {
        try {
            if (query == null || query.trim().isEmpty()) {
                return Collections.emptyList();
            }

            String normalizedQuery = query.toLowerCase().trim();

            List<PlaylistEntity> playlists = playlistRepository
                    .findByNameContainingIgnoreCase(normalizedQuery)
                    .stream()
                    .limit(limit)
                    .collect(Collectors.toList());

            return playlists.stream()
                    .map(playlistConverter::toDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error searching playlists: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

}
