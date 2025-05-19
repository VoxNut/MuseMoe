package com.javaweb.api;

import com.javaweb.model.dto.AlbumDTO;
import com.javaweb.model.request.AlbumRequestDTO;
import com.javaweb.service.AlbumService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/albums")
@RequiredArgsConstructor
@Slf4j
public class AlbumAPI {

    private final AlbumService albumService;

    @PostMapping("/create")
    public ResponseEntity<?> createAlbum(@ModelAttribute AlbumRequestDTO albumRequestDTO) {
        try {
            AlbumDTO res = albumService.createAlbum(albumRequestDTO);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Album created successfully",
                    "artist", res
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "failed", false,
                    "message", "Failed to create album: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<AlbumDTO>> search(
            @RequestParam String query,
            @RequestParam(defaultValue = "20") int limit) {
        try {
            List<AlbumDTO> results = albumService.searchAlbums(query, limit);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("Error searching albums", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/recommendations")
    public ResponseEntity<List<AlbumDTO>> getRecommendedAlbums(
            @RequestParam(defaultValue = "8") int limit) {
        try {
            List<AlbumDTO> recommendations = albumService.getRecommendedAlbums(limit);
            return ResponseEntity.ok(recommendations);
        } catch (Exception e) {
            log.error("Error fetching recommended albums", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/album-by-song")
    public ResponseEntity<AlbumDTO> getAlbumContainsThisSong(@RequestParam Long songId) {
        try {
            AlbumDTO recommendations = albumService.getAlbumContainsThisSong(songId);
            return ResponseEntity.ok(recommendations);
        } catch (Exception e) {
            log.error("Error fetching recommended albums", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/albums-by-artist")
    public ResponseEntity<List<AlbumDTO>> getAlbumsByArtistId(@RequestParam Long artistId) {
        try {
            List<AlbumDTO> res = albumService.getAlbumsByArtistId(artistId);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/album")
    public ResponseEntity<AlbumDTO> fetchAlbumById(@RequestParam Long albumId) {
        try {
            AlbumDTO res = albumService.fetchAlbumById(albumId);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
