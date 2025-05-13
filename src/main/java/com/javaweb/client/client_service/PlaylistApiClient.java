package com.javaweb.client.client_service;

import com.javaweb.model.dto.PlaylistDTO;

import java.util.List;

public interface PlaylistApiClient {

    List<PlaylistDTO> findPlaylistByUserId();


    List<PlaylistDTO> findAllPlaylists();


    boolean addSongToPlaylist(Long playlistId, Long songId);

    PlaylistDTO createPlaylist(String name, Long songId);


    List<PlaylistDTO> searchPlaylists(String query, int limit);

}
