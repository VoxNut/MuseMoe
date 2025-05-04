package com.javaweb.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Set;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class AlbumDTO extends AbstractDTO {
    private Set<String> artistNames;
    private String title;
    private Integer releaseYear;
    private StreamingMediaDTO albumCover;
    private Set<SongDTO> songs;
}
