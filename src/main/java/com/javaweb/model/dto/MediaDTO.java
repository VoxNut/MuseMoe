package com.javaweb.model.dto;


import com.javaweb.enums.MediaType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MediaDTO extends AbstractDTO<MediaDTO>{

    private String fileUrl;

    private MediaType fileType;

    private Integer fileSize;


}
