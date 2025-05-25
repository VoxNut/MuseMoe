package com.javaweb.model.dto;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.javaweb.enums.PlaylistSourceType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PlaylistDTO extends AbstractDTO {

    private String name;
    private String description;
    private String coverImage;
    private Integer totalDuration;
    private List<SongDTO> songs;
    private List<Long> songIds;
    private PlaylistSourceType sourceType;


    public SongDTO getSongAt(Integer position) {
        return songs.get(position);
    }

    public Integer size() {
        return songs.size();
    }

    @JsonIgnore
    public boolean isEmptyPlaylist() {
        return songs != null && songs.isEmpty();
    }

    @JsonIgnore
    public int getRandomSongIndex() {
        return (int) (Math.random() * size());
    }

    @JsonIgnore
    public SongDTO getFirstSong() {
        return songs.getFirst();
    }


    public int getIndexFromSong(SongDTO song) {
        if (song.getPosition() != null) {
            for (int i = 0; i < songs.size(); i++) {
                if (songs.get(i).getPosition() != null &&
                        songs.get(i).getPosition().equals(song.getPosition())) {
                    return i;
                }
            }
        }
        for (int i = 0; i < songs.size(); i++) {
            if (songs.get(i).equals(song)) {
                return i;
            }
        }
        return -1;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || o.getClass() != this.getClass()) return false;

        PlaylistDTO that = (PlaylistDTO) o;
        return this.getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getId());
    }


}
