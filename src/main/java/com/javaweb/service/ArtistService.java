package com.javaweb.service;

import com.javaweb.model.dto.ArtistDTO;
import com.javaweb.model.request.ArtistRequestDTO;

import java.util.List;

public interface ArtistService {
    ArtistDTO createArtist(ArtistRequestDTO artistRequestDTO);

    List<Long> getArtistsIdBySongId(Long songId);

}
