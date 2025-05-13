package com.javaweb.api;


import com.javaweb.model.dto.PlaylistDTO;
import com.javaweb.service.PlaylistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/playlists")
@RequiredArgsConstructor
public class PlaylistAPI {

    private final PlaylistService playlistService;


    @GetMapping
    public ResponseEntity<List<PlaylistDTO>> findPlaylistByUserId() {
        try {
            List<PlaylistDTO> res = playlistService.findPlaylistsByUserId();
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<PlaylistDTO>> findAllPlaylists() {
        try {
            List<PlaylistDTO> res = playlistService.findAllPlaylists();
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<PlaylistDTO>> search(
            @RequestParam String query,
            @RequestParam(defaultValue = "20") int limit) {
        try {
            List<PlaylistDTO> results = playlistService.searchPlaylists(query, limit);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/add_to_playlist")
    public ResponseEntity<Boolean> addSongToPlaylist(@RequestBody Map<String, Object> request) {

        Long playlistId = request.containsKey("playlistId") ? Long.valueOf(request.get("playlistId").toString()) : null;
        Long songId = request.containsKey("songId") ? Long.valueOf(request.get("songId").toString()) : null;

        boolean success = playlistService.addSongToPlaylist(playlistId, songId);
        return ResponseEntity.ok(success);
    }

    @PostMapping("/create")
    public ResponseEntity<PlaylistDTO> createPlaylist(@RequestBody Map<String, Object> request) {
        String name = (String) request.get("name");
        Long songId = request.containsKey("songId") ? Long.valueOf(request.get("songId").toString()) : null;

        PlaylistDTO playlist = playlistService.createPlaylist(name, songId);
        return ResponseEntity.ok(playlist);
    }

}
