package com.javaweb.model.dto;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Objects;

@Getter
@Setter
public class PlaylistDTO extends AbstractDTO<PlaylistDTO> {

    private String user;
    private String name;
    private String description;
    private String coverImage;
    private String totalDuration;
    private List<SongDTO> songs;


    public SongDTO getSongAt(Integer position) {
        return songs.get(position);
    }

    public Integer size() {
        return songs.size();
    }

    @JsonIgnore
    public boolean isEmptyList() {
        return songs.isEmpty();
    }

    @JsonIgnore
    public int getRandomSongIndex() {
        return (int) (Math.random() * size());
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
