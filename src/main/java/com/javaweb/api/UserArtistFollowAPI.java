package com.javaweb.api;

import com.javaweb.model.dto.ArtistDTO;
import com.javaweb.model.dto.UserDTO;
import com.javaweb.service.UserArtistFollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/follows")
@RequiredArgsConstructor
public class UserArtistFollowAPI {

    private final UserArtistFollowService userArtistFollowService;

    @GetMapping("/artists")
    public ResponseEntity<List<ArtistDTO>> findFollowedArtists() {
        try {
            List<ArtistDTO> artists = userArtistFollowService.findFollowedArtists();
            return ResponseEntity.ok(artists);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/artists/{artistId}/followers")
    public ResponseEntity<List<UserDTO>> findFollowersByArtistId(@PathVariable Long artistId) {
        try {
            List<UserDTO> followers = userArtistFollowService.findFollowersByArtistId(artistId);
            return ResponseEntity.ok(followers);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/artists/{artistId}")
    public ResponseEntity<Boolean> followArtist(@PathVariable Long artistId) {
        try {
            boolean result = userArtistFollowService.followArtist(artistId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/artists")
    public ResponseEntity<?> followArtists(@RequestBody List<Long> artistIds) {
        try {
            return ResponseEntity.ok().body(Map.of(
                            "success: ", userArtistFollowService.followArtists(artistIds),
                            "message: ", "Successfully followed artists"
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.ok().body(Map.of(
                            "success: ", userArtistFollowService.followArtists(artistIds),
                            "message: ", "Failed to follow artists: " + e.getMessage()
                    )
            );
        }
    }

    @DeleteMapping("/artists/{artistId}")
    public ResponseEntity<Boolean> unfollowArtist(@PathVariable Long artistId) {
        try {
            boolean result = userArtistFollowService.unfollowArtist(artistId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


}