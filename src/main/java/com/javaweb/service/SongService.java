package com.javaweb.service;


import com.javaweb.model.dto.SongDTO;
import com.javaweb.model.request.SongRequestDTO;

import java.util.List;
import java.util.Map;

public interface SongService {

    SongDTO findOneByTitle(String title);

    SongDTO findById(Long id);


    List<SongDTO> findAllSongsLike(String title);


    SongDTO findSongByUrl(String songUrl);

    List<SongDTO> findAllSongs();

    boolean createSong(SongRequestDTO songRequestDTO);


    int importSongsFromGoogleDrive();

    Map<String, Object> createMultipleSongs(SongRequestDTO songRequestDTO);

    SongDTO findByGoogleDriveId(String driveId);

    List<SongDTO> searchSongs(String query, int limit);

    List<SongDTO> fetchPopularTracksByArtistId(Long artistId, int limit);

    List<SongDTO> fetchSongsByArtist(String artistName, int limit);

    List<SongDTO> findTopByPlayCount(int limit);

    List<SongDTO> findFilteredSongs(Integer year, String genre, Long artistId);

}
