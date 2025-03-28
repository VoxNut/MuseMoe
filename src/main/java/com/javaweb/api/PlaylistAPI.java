package com.javaweb.api;


import com.javaweb.model.dto.PlaylistDTO;
import com.javaweb.service.PlaylistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

}
