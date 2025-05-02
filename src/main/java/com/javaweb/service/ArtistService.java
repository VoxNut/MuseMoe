package com.javaweb.service;

import com.javaweb.model.request.ArtistRequestDTO;

public interface ArtistService {
    boolean createArtist(ArtistRequestDTO artistRequestDTO);
}
