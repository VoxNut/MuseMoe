package com.javaweb.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SongLikesDTO extends AbstractDTO<SongLikesDTO> {
    private UserDTO userDTO;
    private SongDTO songDTO;
}
