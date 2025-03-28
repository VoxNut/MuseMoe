package com.javaweb.utils.client.client_service;

import com.javaweb.model.dto.PlaylistDTO;

import java.util.List;

public interface PlaylistApiClient {

    List<PlaylistDTO> findPlaylistByUserId();


    List<PlaylistDTO> findAllPlaylists();
}
