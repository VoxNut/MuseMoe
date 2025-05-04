package com.javaweb.model.dto;


import com.javaweb.enums.MediaType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class StreamingMediaDTO extends AbstractDTO {

    private String fileUrl;

    private MediaType fileType;

    private Integer fileSize;


}
