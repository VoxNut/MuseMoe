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

public class SongRequestDTO extends AbstractDTO {
    private String googleDriveFileId;

    private Long albumId;
    private Set<Long> artistIds;
    private MultipartFile mp3File;


}
