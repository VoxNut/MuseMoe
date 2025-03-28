package com.javaweb.api;

import com.javaweb.model.dto.SongDTO;
import com.javaweb.service.SongService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/songs")
@RequiredArgsConstructor
public class SongAPI {

    private final SongService songService;
    private final Logger logger = LoggerFactory.getLogger(SongAPI.class);

    @GetMapping("/title/{title}")
    public ResponseEntity<SongDTO> findByTitle(@PathVariable String title) {
        logger.info("Fetching song with title: {}", title);
        try {
            SongDTO song = songService.findOneByTitle(title);
            return ResponseEntity.ok(song);
        } catch (Exception e) {
            logger.error("Failed to fetch song with title: {}", title, e);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<SongDTO> findById(@PathVariable Long id) {
        logger.info("Fetching song with ID: {}", id);
        try {
            SongDTO song = songService.findById(id);
            return ResponseEntity.ok(song);
        } catch (Exception e) {
            logger.error("Failed to fetch song with ID: {}", id, e);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/songs_like/{title}")
    public ResponseEntity<List<SongDTO>> findAllSongsLike(@PathVariable String title) {
        logger.info("Find songs like: {}", title);
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
}
