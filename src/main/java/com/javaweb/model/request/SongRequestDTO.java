package com.javaweb.model.request;

import com.javaweb.model.dto.AbstractDTO;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class SongRequestDTO extends AbstractDTO<SongRequestDTO> {

    private String file_url;
    private AlbumRequestDTO albumRequestDTO;


}
