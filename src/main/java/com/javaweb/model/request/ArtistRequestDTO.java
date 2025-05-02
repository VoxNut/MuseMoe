package com.javaweb.model.request;

import com.javaweb.model.dto.AbstractDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ArtistRequestDTO extends AbstractDTO<ArtistRequestDTO> {
    private String stageName;
    private String bio;
    private String profilePicture;
    private Long userId;
}
