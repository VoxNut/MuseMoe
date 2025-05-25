package com.javaweb.client.client_service;


import com.javaweb.model.dto.SongDTO;
import com.javaweb.model.request.SongRequestDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


public interface SongApiClient {

    SongDTO fetchSongByTitle(String title);

    List<SongDTO> findSongsLike(String title);

    SongDTO findSongByUrl(String fileUrl);

    List<SongDTO> findAllSongs();


    List<SongDTO> searchSongs(String query);

    SongDTO fetchSongById(Long id);

    Boolean createSong(SongRequestDTO songRequestDTO);

    Boolean updateSong(Long id, String title, List<Long> artistIds, MultipartFile file);

    Boolean deleteSong(Long id);

    SongDTO fetchSongByGoogleDriveId(String googleDriveId);

    List<SongDTO> fetchRecommendedSongs(int limit);

    List<SongDTO> search(String query, int limit);

    List<SongDTO> fetchPopularTracksByArtistId(Long artistId);

    List<SongDTO> fetchSongsByArtist(String artistName, int limit);

    Boolean createSongs(SongRequestDTO songRequestDTO);

    List<SongDTO> findTopByPlayCount(Integer limit);

    List<SongDTO> findSongsByFilter(Integer releaseYear, String genre, Long artistId);

}
