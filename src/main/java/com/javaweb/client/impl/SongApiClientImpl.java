package com.javaweb.client.impl;

import com.javaweb.client.ApiConfig;
import com.javaweb.client.client_service.SongApiClient;
import com.javaweb.model.dto.SongDTO;
import com.javaweb.model.request.SongRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
class SongApiClientImpl implements SongApiClient {
    private final ApiClient apiClient;
    private final UrlEncoder urlEncoder;
    private final ApiConfig apiConfig;


    @Override
    public SongDTO fetchSongById(Long id) {
        try {
            String url = apiConfig.buildSongUrl("/" + id.toString());
            SongDTO songDTO = apiClient.get(url, SongDTO.class);
            return songDTO;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<SongDTO> search(String query, int limit) {
        try {
            String url = apiConfig.buildSongUrl("/search?query=" + query + "&limit=" + limit);
            return apiClient.getList(url, SongDTO.class);
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public List<SongDTO> fetchRecommendedSongs(int limit) {
        try {
            String url = apiConfig.buildRecommendationsUrl("?limit=" + limit);
            List<SongDTO> recommendedSongs = apiClient.getList(url, SongDTO.class);
            return recommendedSongs;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public Boolean createSong(SongRequestDTO songRequestDTO) {
        try {
            String url = apiConfig.buildSongUrl("/create");

            Map<String, Object> parts = new HashMap<>();
            parts.put("albumId", songRequestDTO.getAlbumId());

            if (songRequestDTO.getArtistIds() != null && !songRequestDTO.getArtistIds().isEmpty()) {
                for (int i = 0; i < songRequestDTO.getArtistIds().size(); i++) {
                    parts.put("artistIds[" + i + "]", songRequestDTO.getArtistIds().get(i));
                }
            }

            if (songRequestDTO.getMp3Files() != null && !songRequestDTO.getMp3Files().isEmpty()) {
                parts.put("mp3Files", songRequestDTO.getMp3Files());
            }

            Boolean result = apiClient.postMultipart(url, parts, Boolean.class);
            return result;
        } catch (Exception e) {
            log.error("Error creating song: {}", e.getMessage(), e);
            return false;
        }
    }

    public Boolean createSongs(SongRequestDTO songRequestDTO) {
        try {
            String url = apiConfig.buildSongUrl("/create-multiple");

            Map<String, Object> parts = new HashMap<>();
            parts.put("albumId", songRequestDTO.getAlbumId());

            if (songRequestDTO.getArtistIds() != null && !songRequestDTO.getArtistIds().isEmpty()) {
                for (int i = 0; i < songRequestDTO.getArtistIds().size(); i++) {
                    parts.put("artistIds[" + i + "]", songRequestDTO.getArtistIds().get(i));
                }
            }

            if (songRequestDTO.getMp3Files() != null) {
                parts.put("mp3Files", songRequestDTO.getMp3Files());
            }

            Boolean result = apiClient.postMultipart(url, parts, Boolean.class);
            return result;
        } catch (Exception e) {
            log.error("Error creating songs: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public Boolean updateSong(Long id, String title, List<Long> artistIds, MultipartFile file) {
        return null;
    }

    @Override
    public Boolean deleteSong(Long id) {
        return null;
    }

    @Override
    public SongDTO fetchSongByGoogleDriveId(String googleDriveId) {
        try {
            String url = apiConfig.buildSongUrl("/driveId?driveId=" + googleDriveId);
            SongDTO res = apiClient.get(url, SongDTO.class);
            return res;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public SongDTO fetchSongByTitle(String title) {
        try {

            String encodedTitle = urlEncoder.encode(title);
            String url = apiConfig.buildSongUrl("/title/" + encodedTitle);
            return apiClient.get(url, SongDTO.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<SongDTO> findSongsLike(String title) {
        try {
            String url = apiConfig.buildSongUrl("/songs_like?title=" + title);
            List<SongDTO> songDTOS = apiClient.getList(url, SongDTO.class);
            return songDTOS;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public SongDTO findSongByUrl(String fileUrl) {
        try {
            String encodedFileUrl = urlEncoder.encode(fileUrl);
            String url = apiConfig.buildSongUrl("/url?songUrl=" + encodedFileUrl);
            SongDTO songDTO = apiClient.get(url, SongDTO.class);
            return songDTO;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<SongDTO> findAllSongs() {
        try {
            String url = apiConfig.buildSongUrl("/all");
            List<SongDTO> songs = apiClient.getList(url, SongDTO.class);
            return songs;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public List<SongDTO> fetchPopularTracksByArtistId(Long artistId) {
        try {
            String url = apiConfig.buildSongUrl("/popular_tracks_by_artist?artistId=" + artistId);
            List<SongDTO> songs = apiClient.getList(url, SongDTO.class);
            return songs;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public List<SongDTO> searchSongs(String query) {
        return List.of();
    }

    @Override
    public List<SongDTO> fetchSongsByArtist(String artistName, int limit) {
        try {
            String url = apiConfig.buildSongUrl("/artist?artistName=" + artistName + "&limit=" + limit);
            List<SongDTO> songs = apiClient.getList(url, SongDTO.class);
            return songs;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}