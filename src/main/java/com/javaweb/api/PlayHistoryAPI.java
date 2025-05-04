package com.javaweb.api;


import com.javaweb.model.dto.PlayHistoryDTO;
import com.javaweb.model.dto.SongDTO;
import com.javaweb.service.PlayHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api/play_history")
@RequiredArgsConstructor
public class PlayHistoryAPI {

    private final PlayHistoryService playHistoryService;

    @PostMapping("/create")
    public ResponseEntity<Boolean> createNewPlayHistory(@RequestBody PlayHistoryDTO playHistoryDTO) {
        try {
            boolean res = playHistoryService.createNewPlayHistory(playHistoryDTO.getSongId());
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/recent_songs")
    public ResponseEntity<List<SongDTO>> fetchRecentPlayHistory(@Param("limit") Integer limit) {

        try {
            List<SongDTO> recentSongs = playHistoryService.fetchRecentPlayHistory(limit);
            return ResponseEntity.ok(recentSongs);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/clear-songs")
    public ResponseEntity<Boolean> clearPlayHistorySongs(@RequestBody List<Long> songIds) {
        try {
            boolean result = playHistoryService.clearPlayHistoryBySongs(songIds);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/clear-all")
    public ResponseEntity<Boolean> clearAllPlayHistory() {
        try {
            boolean result = playHistoryService.clearAllPlayHistory();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
