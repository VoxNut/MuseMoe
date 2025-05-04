package com.javaweb.service;

import com.javaweb.model.dto.AlbumDTO;
import com.javaweb.model.request.AlbumRequestDTO;

public interface AlbumService {
    AlbumDTO createAlbum(AlbumRequestDTO albumRequestDTO);
}
