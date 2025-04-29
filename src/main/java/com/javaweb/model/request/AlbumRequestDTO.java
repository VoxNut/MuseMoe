package com.javaweb.model.request;

import com.javaweb.model.dto.AbstractDTO;

import java.util.Date;
import java.util.Set;

public class AlbumRequestDTO extends AbstractDTO<AlbumRequestDTO> {

    private String title;
    private Date releaseDate;
    private String coverArt;
    private Set<SongRequestDTO> songRequestDTOSet;
}
