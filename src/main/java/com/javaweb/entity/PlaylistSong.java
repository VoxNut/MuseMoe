package com.javaweb.entity;


import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "playlist_song")
@Getter
@Setter
public class PlaylistSong extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "playlist_id")
    private Playlist playlist;

    @ManyToOne
    @JoinColumn(name = "song_id")
    private Song song;


    @Column(name = "added_at")
    private LocalDateTime addedAt;

    @Column(name = "position")
    private Integer position;
}
