package com.javaweb.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_downloads")
@Getter
@Setter
public class UserDownloadEntity {

    @EmbeddedId
    private UserDownloadId id;

    @Column(name = "download_date")
    private LocalDateTime downloadDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("songId")
    @JoinColumn(name = "song_id")
    private SongEntity song;


    public UserDownloadEntity() {
    }

    public UserDownloadEntity(UserEntity user, SongEntity song) {
        this.user = user;
        this.song = song;
        this.id = new UserDownloadId(user.getId(), song.getId());
        this.downloadDate = LocalDateTime.now();
    }

}