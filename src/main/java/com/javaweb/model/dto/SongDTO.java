package com.javaweb.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mpatric.mp3agic.Mp3File;
import lombok.Getter;
import lombok.Setter;

import java.awt.image.BufferedImage;
import java.util.Date;
import java.util.Objects;

@Getter
@Setter
public class SongDTO extends AbstractDTO<SongDTO> {

    private String songTitle;
    private String songArtist;
    private String album;
    private String songLength;
    private String genre;
    private Date releaseDate;
    private String audioFilePath;
    private double frameRatePerMilliseconds;

    @JsonIgnore
    private Mp3File mp3File;
    @JsonIgnore
    private BufferedImage songImage;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || (o.getClass() != this.getClass())) return false;

        SongDTO that = (SongDTO) o;

        return this.songTitle.equals(that.songTitle)
                && this.audioFilePath.equals(that.audioFilePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(songTitle, songArtist, audioFilePath);
    }


}
