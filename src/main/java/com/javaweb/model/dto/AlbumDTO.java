package com.javaweb.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Set;


@Getter
@Setter
public class AlbumDTO {
    private ArtistDTO artistDTO;
    private String title;
    private Date releaseDate;
    private MediaDTO albumCover;
    private Set<SongDTO> songs;
}
