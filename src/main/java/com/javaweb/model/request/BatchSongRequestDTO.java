package com.javaweb.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BatchSongRequestDTO {
    private Long albumId;
    private Set<Long> artistIds;
    private List<MultipartFile> mp3Files;
}