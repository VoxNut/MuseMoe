package com.javaweb.api;

import com.javaweb.model.request.ArtistRequestDTO;
import com.javaweb.service.ArtistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/artists")
@RequiredArgsConstructor
public class ArtistAPI {
    
    private final ArtistService artistService;

    @PostMapping("/create")
    public ResponseEntity<Boolean> createNewArtist(@RequestBody ArtistRequestDTO artistRequestDTO) {
        try {
            boolean res = artistService.createArtist(artistRequestDTO);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return ResponseEntity.ok(false);
        }
    }
}
