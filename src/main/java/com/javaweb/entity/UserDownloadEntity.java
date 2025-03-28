package com.javaweb.entity;


import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
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

    @Column(name = "download_count")
    private Integer downloadCount = 1;

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

    public void incrementDownloadCount() {
        this.downloadCount += 1;
    }
}