package com.javaweb.model.request;

import com.javaweb.model.dto.AbstractDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class AlbumRequestDTO extends AbstractDTO<AlbumRequestDTO> {

    private String title;
    private Integer releaseYear;

    private String coverArtPath;
    private Long artistId;
    private Set<SongRequestDTO> songRequestDTOSet;
}
