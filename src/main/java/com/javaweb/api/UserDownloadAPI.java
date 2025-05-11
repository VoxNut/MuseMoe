package com.javaweb.api;

import com.javaweb.model.dto.SongDTO;
import com.javaweb.service.UserDownloadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user_downloads")
@RequiredArgsConstructor
public class UserDownloadAPI {

    private final UserDownloadService userDownloadService;

    @GetMapping("/songs")
    public ResponseEntity<List<SongDTO>> findUserDownloadedSongs() {
        try {
            List<SongDTO> res = userDownloadService.findAllDownloadedSongs();
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/create")
    public ResponseEntity<Boolean> createNewUserDownload(@RequestBody SongDTO songDTO) {
        try {
            boolean res = userDownloadService.createNewUserDownload(songDTO.getId());
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

}
