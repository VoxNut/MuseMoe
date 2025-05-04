package com.javaweb.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.awt.image.BufferedImage;
import java.util.Set;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ArtistDTO extends AbstractDTO {

    private String stageName;
    private String bio;
    private String profilePictureId;
    private Set<AlbumDTO> albumDTOS;
    private Set<SongDTO> songDTOSet;
    @JsonIgnore
    private BufferedImage profileImage;

}
