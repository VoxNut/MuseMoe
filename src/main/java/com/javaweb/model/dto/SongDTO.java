package com.javaweb.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.awt.image.BufferedImage;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class SongDTO extends AbstractDTO {

    private String title;
    private String songArtist;
    private String songAlbum;
    private String songLength;
    private Integer duration;
    private Long frame;
    private Long bitrate;
    private String genre;
    private Integer releaseYear;
    private String songLyrics;
    private Integer playCount;

    private double frameRatePerMilliseconds;
    private int lengthInMilliseconds;

    private Integer position;


    private String driveFileId;
    private String webContentLink;

    private String albumArtId;

    private String localFilePath;
    private Boolean isLocalFile = false;
    private Date downloadDate;
    @JsonIgnore
    private BufferedImage songImage;
    private Long albumId;
    private List<Long> artistIds;
    private List<ArtistDTO> artistDTOs;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || (o.getClass() != this.getClass())) return false;

        SongDTO that = (SongDTO) o;

        return this.title.equals(that.title)
                && this.driveFileId.equals(that.driveFileId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, songArtist, driveFileId);
    }


}
