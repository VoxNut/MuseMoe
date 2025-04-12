package com.javaweb.api;


import com.javaweb.service.SongLikesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/song_likes")
@RequiredArgsConstructor
public class SongLikesAPI {


    private final SongLikesService songLikesService;

    @PostMapping("/song_id")
    public ResponseEntity<Boolean> createNewSongLikes(@RequestParam("songId") Long songId) {
        try {
            boolean res = songLikesService.createSongLikes(songId);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok().build();
        }
    }

    @GetMapping("/song_liked")
    public ResponseEntity<Boolean> checkSongLiked(@RequestParam("songId") Long songId) {
        try {
            Boolean res = songLikesService.checkSongLiked(songId);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/delete/{songId}")
    public ResponseEntity<Boolean> deleteSongLikes(@PathVariable("songId") Long songId) {
        try {
            Boolean res = songLikesService.deleteSongLikes(songId);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }


}
