package com.javaweb.model.request;

import com.javaweb.model.dto.AbstractDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ArtistRequestDTO extends AbstractDTO {
    private String stageName;
    private String bio;
    private MultipartFile artistProfilePicture;
    private Long userId;
    private String googleDriveFileId;
}
