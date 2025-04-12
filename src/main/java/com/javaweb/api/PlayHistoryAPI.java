package com.javaweb.api;


import com.javaweb.service.PlayHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/api/play_history")
@RequiredArgsConstructor
public class PlayHistoryAPI {

    private final PlayHistoryService playHistoryService;

    @PostMapping("/create")
    public ResponseEntity<Boolean> createNewPlayHistory(@RequestParam("songId") Long songId) {
        try {
            boolean res = playHistoryService.createNewPlayHistory(songId);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
