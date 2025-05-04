package com.javaweb.model.request;

import com.javaweb.model.dto.AbstractDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AlbumRequestDTO extends AbstractDTO {

    private String title;
    private Integer releaseYear;
    private MultipartFile albumCover;
    private String googleDriveFileId;
    private Long artistId;
    private Set<SongRequestDTO> songRequestDTOSet;
}
