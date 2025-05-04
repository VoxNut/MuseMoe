package com.javaweb.service;

import com.javaweb.model.dto.ArtistDTO;
import com.javaweb.model.request.ArtistRequestDTO;

public interface ArtistService {
    ArtistDTO createArtist(ArtistRequestDTO artistRequestDTO);
}
