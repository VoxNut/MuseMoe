package com.javaweb.client.impl;

import com.javaweb.client.ApiConfig;
import com.javaweb.client.client_service.SongApiClient;
import com.javaweb.model.dto.SongDTO;
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

    public Boolean createSong(Long albumId, String title, List<Long> artistIds, MultipartFile file) {
        try {
            String url = apiConfig.buildSongUrl("/create");

            Map<String, Object> parts = new HashMap<>();
            parts.put("albumId", albumId);
            parts.put("artistIds", artistIds);

            if (file != null) {
                parts.put("mp3File", file);
            }

            Boolean result = apiClient.postMultipart(url, parts, Boolean.class);
            return result;
        } catch (Exception e) {
            log.error("Error creating song", e);
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
    public List<SongDTO> searchSongs(String query) {
        return List.of();
    }


}