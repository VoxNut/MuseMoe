package com.javaweb.api;

import com.javaweb.model.dto.ArtistDTO;
import com.javaweb.model.request.ArtistRequestDTO;
import com.javaweb.service.ArtistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/artists")
@RequiredArgsConstructor
@Slf4j
public class ArtistAPI {

    private final ArtistService artistService;

    @PostMapping(value = "/create")
    public ResponseEntity<?> createNewArtist(@ModelAttribute ArtistRequestDTO artistRequestDTO) {

        try {
            ArtistDTO createdArtist = artistService.createArtist(artistRequestDTO);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Artist profile created successfully",
                    "artist", createdArtist
            ));
        } catch (Exception e) {
            log.error("Failed to create artist profile", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Failed to create artist profile: " + e.getMessage()
            ));
        }
    }


}