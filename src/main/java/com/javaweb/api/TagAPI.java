package com.javaweb.api;


import com.javaweb.service.TagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
@Slf4j
public class TagAPI {

    private final TagService aiTagService;

    @PostMapping("/auto-tag")
    public ResponseEntity<Boolean> autoTagSongs(@RequestBody List<Long> songIds) {
        try {
            log.info("Request to auto-tag songs: {}", songIds);
            aiTagService.autoTagSongs(songIds);
            return ResponseEntity.ok(true);
        } catch (Exception e) {
            log.error("Error auto-tagging songs: {}", e.getMessage(), e);
            return ResponseEntity.ok(false);
        }
    }

    @PostMapping("/single/{songId}")
    public ResponseEntity<Boolean> autoTagSong(@PathVariable Long songId) {
        try {
            log.info("Request to auto-tag song ID: {}", songId);
            aiTagService.autoTagSongs(List.of(songId));
            return ResponseEntity.ok(true);
        } catch (Exception e) {
            log.error("Error auto-tagging song: {}", e.getMessage(), e);
            return ResponseEntity.ok(false);
        }
    }
}