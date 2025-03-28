package com.javaweb.model.request;

import java.util.Date;
import java.util.Set;

public class AlbumRequestDTO {

    private String title;
    private Date releaseDate;
    private String coverArt;
    private Set<SongRequestDTO> songRequestDTOSet;
}
