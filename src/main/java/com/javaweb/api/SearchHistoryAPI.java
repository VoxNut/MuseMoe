package com.javaweb.api;

import com.javaweb.model.dto.SongDTO;
import com.javaweb.service.SearchHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search_history")
@RequiredArgsConstructor
public class SearchHistoryAPI {

    private final SearchHistoryService searchHistoryService;

    @PostMapping("/log")
    public ResponseEntity<Boolean> logSearchHistory(@RequestParam("songId") Long songId,
                                                    @RequestParam("searchTerm") String searchTerm) {
        try {
            boolean result = searchHistoryService.logSearchHistory(songId, searchTerm);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/recent_songs")
    public ResponseEntity<List<SongDTO>> fetchRecentSearchHistory(@RequestParam(value = "limit", defaultValue = "10") Integer limit) {
        try {
            List<SongDTO> recentSearches = searchHistoryService.fetchRecentSearchHistory(limit);
            return ResponseEntity.ok(recentSearches);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/recent_terms")
    public ResponseEntity<List<String>> fetchRecentSearchTerms(@RequestParam(value = "limit", defaultValue = "10") Integer limit) {
        try {
            List<String> searchTerms = searchHistoryService.fetchRecentSearchTerms(limit);
            return ResponseEntity.ok(searchTerms);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/clear-songs")
    public ResponseEntity<Boolean> clearSearchHistory(@RequestBody List<Long> songIds) {
        try {
            boolean result = searchHistoryService.clearSearchHistoryBySongs(songIds);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/clear-all")
    public ResponseEntity<Boolean> clearAllSearchHistory() {
        try {
            boolean result = searchHistoryService.clearAllSearchHistory();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}