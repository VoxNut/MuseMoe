package com.javaweb.model.request;

import com.javaweb.model.dto.AbstractDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;


@Getter
@Setter
public class SongRequestDTO extends AbstractDTO<SongRequestDTO> {

    private String file_url;
    private AlbumRequestDTO albumRequestDTO;
    private Set<ArtistRequestDTO> artistRequestDTOS;


}
