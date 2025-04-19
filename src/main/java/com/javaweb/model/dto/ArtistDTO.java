package com.javaweb.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;


@Getter
@Setter
public class ArtistDTO extends AbstractDTO<ArtistDTO> {

    private String stageName;
    private String bio;
    private String profilePicture;
    private Set<AlbumDTO> albumDTOS;
    private Set<SongDTO> songDTOSet;
}
