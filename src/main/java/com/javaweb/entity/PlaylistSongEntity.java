package com.javaweb.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "playlist_song")
@Getter
@Setter
public class PlaylistSongEntity {

    @EmbeddedId
    private PlaylistSongId id;


    @Column(name = "added_at")
    private LocalDateTime addedAt;

    @Column(name = "position")
    private Integer position;


    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("playlistId")
    @JoinColumn(name = "playlist_id")
    private PlaylistEntity playlist;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("songId")
    @JoinColumn(name = "song_id")
    private SongEntity song;


}
