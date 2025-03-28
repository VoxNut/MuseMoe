package com.javaweb.service;

import com.javaweb.model.dto.PlaylistDTO;

import java.util.List;

public interface PlaylistService {

    List<PlaylistDTO> findPlaylistsByUserId();

    List<PlaylistDTO> findAllPlaylists();
}
