package com.javaweb.client.client_service;

import com.javaweb.model.dto.AlbumDTO;

import java.util.List;

public interface AlbumApiClient {
    List<AlbumDTO> searchAlbums(String query, int limit);

}
