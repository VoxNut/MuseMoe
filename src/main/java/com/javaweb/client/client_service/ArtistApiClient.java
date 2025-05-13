package com.javaweb.client.client_service;

import com.javaweb.model.dto.ArtistDTO;

import java.util.List;

public interface ArtistApiClient {
    List<ArtistDTO> searchArtists(String query, int limit);
}
