package com.javaweb.service;

import com.javaweb.model.request.AlbumRequestDTO;

public interface AlbumService {
    boolean createAlbum(AlbumRequestDTO albumRequestDTO);
}
