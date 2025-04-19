package com.javaweb.service;

import com.javaweb.model.dto.ArtistDTO;
import com.javaweb.model.dto.UserDTO;

import java.util.List;

public interface UserArtistFollowService {


    List<ArtistDTO> findFollowedArtists();


    List<UserDTO> findFollowersByArtistId(Long artistId);


    boolean followArtist(Long artistId);


    boolean unfollowArtist(Long artistId);
}
