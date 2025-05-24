package com.javaweb.client.client_service;

import com.javaweb.model.dto.ArtistDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ArtistApiClient {
    List<ArtistDTO> searchArtists(String query, int limit);

    List<ArtistDTO> findArtistsBySongId(Long songId);

    boolean checkArtistFollowed(Long artistId);

    ArtistDTO findArtistById(Long artistId);

    boolean createArtist(String stageName, String bio, MultipartFile artistProfilePicture);


    List<ArtistDTO> findAllArtists();
}
