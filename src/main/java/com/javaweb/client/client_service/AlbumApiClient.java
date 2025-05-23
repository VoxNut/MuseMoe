package com.javaweb.client.client_service;

import com.javaweb.model.dto.AlbumDTO;
import com.javaweb.model.request.AlbumRequestDTO;

import java.util.List;

public interface AlbumApiClient {
    List<AlbumDTO> searchAlbums(String query, int limit);

    List<AlbumDTO> getRecommendedAlbums(int limit);

    AlbumDTO getAlbumContainsThisSong(Long songId);

    List<AlbumDTO> getAlbumsByArtistId(Long artistId);

    AlbumDTO getAlbumById(Long albumId);

    String getAlbumCoverId(Long albumId);

    AlbumDTO createAlbum(AlbumRequestDTO albumRequestDTO);
}
