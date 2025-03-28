package com.javaweb.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;


@Getter
@Setter
public class ArtistDTO extends AbstractDTO<ArtistDTO> {

    private UserDTO userDTO;
    private String bio;
    private MediaDTO profilePic;
    private Set<AlbumDTO> albumDTOS;
    private Set<SongDTO> songDTOSet;
}
