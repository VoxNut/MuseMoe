package com.javaweb.api;

import com.javaweb.model.dto.SongDTO;
import com.javaweb.model.request.SongRequestDTO;
import com.javaweb.service.SongService;
import com.javaweb.service.impl.GoogleDriveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/songs")
@RequiredArgsConstructor
@Slf4j
public class SongAPI {

    private final SongService songService;
    private final GoogleDriveService googleDriveService;

    @GetMapping("/title/{title}")
    public ResponseEntity<SongDTO> findByTitle(@PathVariable String title) {
        log.info("Fetching song with title: {}", title);
        try {
            SongDTO song = songService.findOneByTitle(title);
            return ResponseEntity.ok(song);
        } catch (Exception e) {
            log.error("Failed to fetch song with title: {}", title, e);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<SongDTO> findById(@PathVariable Long id) {
        log.info("Fetching song with ID: {}", id);
        try {
            SongDTO song = songService.findById(id);
            return ResponseEntity.ok(song);
        } catch (Exception e) {
            log.error("Failed to fetch song with ID: {}", id, e);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/songs_like")
    public ResponseEntity<List<SongDTO>> findAllSongsLike(@Param("title") String title) {
        log.info("Find songs like: {}", title);
        try {
            List<SongDTO> songDTOSet = songService.findAllSongsLike(title);
            return ResponseEntity.ok(songDTOSet);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/url")
    public ResponseEntity<SongDTO> findSongByUrl(@RequestParam String songUrl) {
        try {
            SongDTO songDTO = songService.findSongByUrl(songUrl);
            return ResponseEntity.ok(songDTO);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<SongDTO>> findAllSongs() {
        try {
            List<SongDTO> songDTOS = songService.findAllSongs();
            return ResponseEntity.ok(songDTOS);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/driveId")
    public ResponseEntity<SongDTO> getSongByGoogleDriveId(@RequestParam("driveId") String driveId) {
        try {
            SongDTO res = songService.findByGoogleDriveId(driveId);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/import-from-drive")
    public ResponseEntity<Integer> importSongsFromDrive() {
        log.info("Starting import of songs from Google Drive");
        try {
            int importedCount = songService.importSongsFromGoogleDrive();
            return ResponseEntity.ok(importedCount);
        } catch (Exception e) {
            log.error("Failed to import songs from Google Drive", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(0);
        }
    }

    @PostMapping("/create")
    public ResponseEntity<Boolean> createSong(@ModelAttribute SongRequestDTO songRequestDTO) {
        try {
            boolean result = songService.createSong(songRequestDTO);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error uploading song: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }

    @PostMapping("/create-multiple")
    public ResponseEntity<Boolean> createMultipleSongs(@ModelAttribute SongRequestDTO songRequestDTO) {
        try {
            log.info("Received request to upload {} song files to album ID: {}",
                    songRequestDTO.getMp3Files() != null ? songRequestDTO.getMp3Files().size() : 0,
                    songRequestDTO.getAlbumId());

            Map<String, Object> result = songService.createMultipleSongs(songRequestDTO);

            if ((Boolean) result.get("success")) {
                return ResponseEntity.ok(true);
            } else {
                return ResponseEntity.badRequest().body(false);
            }

        } catch (Exception e) {
            log.error("Error processing batch song upload: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<SongDTO>> search(
            @RequestParam String query,
            @RequestParam(defaultValue = "20") int limit) {

        List<SongDTO> results = songService.searchSongs(query, limit);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/popular_tracks_by_artist")
    public ResponseEntity<List<SongDTO>> fetchPopularTracksByArtistId(
            @RequestParam Long artistId,
            @RequestParam(defaultValue = "5") int limit) {

        List<SongDTO> results = songService.fetchPopularTracksByArtistId(artistId, limit);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/artist")
    public ResponseEntity<List<SongDTO>> fetchSongsByArtist(
            @RequestParam String artistName,
            @RequestParam(defaultValue = "5") int limit) {

        List<SongDTO> results = songService.fetchSongsByArtist(artistName, limit);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/top_by_play_count")
    public ResponseEntity<List<SongDTO>> fetchTopSongsByPlayCount(
            @RequestParam(defaultValue = "5") int limit) {
        try {
            List<SongDTO> results = songService.findTopByPlayCount(limit);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


}
