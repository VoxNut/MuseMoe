package com.javaweb.api;

import com.javaweb.model.request.AlbumRequestDTO;
import com.javaweb.service.AlbumService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/albums")
@RequiredArgsConstructor
@Slf4j
public class AlbumAPI {

    private final AlbumService albumService;


    @PostMapping("/create")
    public ResponseEntity<Boolean> createAlbum(@RequestBody AlbumRequestDTO albumRequestDTO) {
        try {
            boolean res = albumService.createAlbum(albumRequestDTO);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return ResponseEntity.ok(false);
        }
    }

}
