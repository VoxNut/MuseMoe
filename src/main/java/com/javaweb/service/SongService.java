package com.javaweb.service;


import com.javaweb.model.dto.SongDTO;

import java.util.List;

public interface SongService {

    SongDTO findOneByTitle(String title);

    SongDTO findById(Long id);


    List<SongDTO> findAllSongsLike(String title);


    SongDTO findSongByUrl(String songUrl);

    List<SongDTO> findAllSongs();
}
