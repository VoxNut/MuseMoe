package com.javaweb.utils.client.client_service;

import com.javaweb.model.dto.SongDTO;

import java.util.List;

public interface UserDownloadApiClient {

    List<SongDTO> findUserDownloadedSongs();
}
