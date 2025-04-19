package com.javaweb.client.client_service;

import com.javaweb.model.dto.ArtistDTO;
import com.javaweb.model.dto.UserDTO;

import java.util.List;

public interface UserArtistFollowApiClient {
    List<ArtistDTO> findFollowedArtists();

    List<UserDTO> findFollowersByArtistId(Long artistId);

    Boolean followArtist(Long artistId);

    Boolean unfollowArtist(Long artistId);
}