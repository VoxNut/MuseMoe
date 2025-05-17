package com.javaweb.service;

import com.javaweb.model.dto.PlaylistDTO;

import java.util.List;

public interface PlaylistService {

    List<PlaylistDTO> findPlaylistsByUserId();

    List<PlaylistDTO> findAllPlaylists();

    boolean createPlaylist(PlaylistDTO playlistDTO);

    boolean updatePlaylist(PlaylistDTO playlistDTO);

    boolean deletePlaylist(Long playlistId);

    PlaylistDTO createPlaylist(String name, Long songId);

    boolean addSongToPlaylist(Long playlistId, List<Long> songIds);

    List<PlaylistDTO> searchPlaylists(String query, int limit);

}