package com.javaweb.client.client_service;


import com.javaweb.model.dto.SongDTO;

import java.util.List;


public interface SongApiClient {

    SongDTO fetchSongByTitle(String title);

    List<SongDTO> findSongsLike(String title);

    SongDTO findSongByUrl(String fileUrl);

    List<SongDTO> findAllSongs();


    List<SongDTO> searchSongs(String query);

    SongDTO fetchSongById(Long id);

}
