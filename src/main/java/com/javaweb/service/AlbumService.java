package com.javaweb.service;

import com.javaweb.model.dto.AlbumDTO;
import com.javaweb.model.request.AlbumRequestDTO;

import java.util.List;

public interface AlbumService {
    AlbumDTO createAlbum(AlbumRequestDTO albumRequestDTO);

    List<AlbumDTO> searchAlbums(String query, int limit);
}
