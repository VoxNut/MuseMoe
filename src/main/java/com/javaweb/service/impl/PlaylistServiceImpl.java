package com.javaweb.service.impl;

import com.javaweb.converter.PlaylistConverter;
import com.javaweb.entity.PlaylistEntity;
import com.javaweb.entity.PlaylistSongEntity;
import com.javaweb.entity.SongEntity;
import com.javaweb.entity.UserEntity;
import com.javaweb.exception.EntityNotFoundException;
import com.javaweb.model.dto.PlaylistDTO;
import com.javaweb.model.dto.SongDTO;
import com.javaweb.repository.PlaylistRepository;
import com.javaweb.repository.SongRepository;
import com.javaweb.repository.UserRepository;
import com.javaweb.service.PlaylistService;
import com.javaweb.service.SongService;
import com.javaweb.service.UserService;
import com.javaweb.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final UserService userService;
    private final SongService songService;

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
            playlist.setPlaylistSongEntities(new ArrayList<>());

            // Save the playlist first to get its ID
            PlaylistEntity savedPlaylist = playlistRepository.save(playlist);

            // If a song was provided, add it to the playlist
            if (songId != null) {
                SongEntity song = songRepository.findById(songId)
                        .orElseThrow(() -> new EntityNotFoundException("Song not found with ID: " + songId));

                PlaylistSongEntity playlistSong = new PlaylistSongEntity(playlist, song);

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
            throw e;
        }
    }


    public boolean addSongToPlaylist(Long playlistId, List<Long> songIds) {
        try {
            log.info("Adding songs {} to playlist {}", songIds, playlistId);

            PlaylistEntity playlist = playlistRepository.findById(playlistId)
                    .orElseThrow(() -> new EntityNotFoundException("Playlist not found " + playlistId));

            Set<Long> existing = playlist.getPlaylistSongEntities().stream()
                    .map(ps -> ps.getSong().getId())
                    .collect(Collectors.toSet());

            for (Long songId : songIds) {
                if (existing.contains(songId)) continue;

                SongEntity song = songRepository.findById(songId)
                        .orElseThrow(() -> new EntityNotFoundException("Song not found " + songId));

                PlaylistSongEntity link = new PlaylistSongEntity(playlist, song, playlist.getPlaylistSongEntities().size() + 1);
                playlist.getPlaylistSongEntities().add(link);
            }

            playlistRepository.save(playlist);
            return true;

        } catch (Exception e) {
            log.error("Error adding songs {} to playlist {}: {}", songIds, playlistId, e.getMessage(), e);
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

            // 1. Direct name matches (highest priority)
            List<PlaylistEntity> directMatches = playlistRepository
                    .findByNameContainingIgnoreCase(normalizedQuery);
            Set<PlaylistEntity> results = new LinkedHashSet<>(directMatches);

            // 2. Find playlists containing songs matching the query
            if (results.size() < limit) {
                List<SongDTO> matchingSongs = songService.searchSongs(normalizedQuery, 50);
                if (!matchingSongs.isEmpty()) {
                    Set<Long> songIds = matchingSongs.stream()
                            .map(SongDTO::getId)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet());

                    if (!songIds.isEmpty()) {
                        List<PlaylistEntity> playlistsWithMatchingSongs =
                                playlistRepository.findPlaylistsContainingSongs(new ArrayList<>(songIds), limit - results.size());
                        results.addAll(playlistsWithMatchingSongs);
                    }
                }
            }

            // 3. Find playlists by creator name/username if it matches the query
            if (results.size() < limit && userService != null) {
                List<UserEntity> matchingUsers = userRepository.findByUsernameContainingIgnoreCase(normalizedQuery);
                if (!matchingUsers.isEmpty()) {
                    for (UserEntity user : matchingUsers) {
                        List<PlaylistEntity> userPlaylists = playlistRepository.findByUser(user);
                        results.addAll(userPlaylists);
                    }
                }
            }

            // Convert to DTOs and apply limit
            return results.stream()
                    .limit(limit)
                    .map(playlistConverter::toDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error searching playlists: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

}
