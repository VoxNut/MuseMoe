package com.javaweb.service;

import com.javaweb.model.dto.AlbumDTO;
import com.javaweb.model.request.AlbumRequestDTO;

import java.util.List;

public interface AlbumService {
    AlbumDTO createAlbum(AlbumRequestDTO albumRequestDTO);

    List<AlbumDTO> searchAlbums(String query, int limit);

    List<AlbumDTO> getRecommendedAlbums(int limit);

    AlbumDTO getAlbumContainsThisSong(Long songId);

    List<AlbumDTO> getAlbumsByArtistId(Long artistId);

    AlbumDTO fetchAlbumById(Long albumId);

    List<AlbumDTO> findAllAlbums();
}
