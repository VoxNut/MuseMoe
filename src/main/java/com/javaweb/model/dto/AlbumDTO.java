package com.javaweb.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.awt.image.BufferedImage;
import java.util.List;
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
    private List<SongDTO> songDTOS;
    @JsonIgnore
    private BufferedImage albumImage;
    private String imageId;
    private String artistName;
    private Long artistId;
    private String albumLength;
    private Integer totalDuration;
    

}
