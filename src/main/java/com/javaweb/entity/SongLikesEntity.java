package com.javaweb.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "song_likes")
@Getter
@Setter
public class SongLikesEntity {

    @EmbeddedId
    private SongLikesId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("songId")
    @JoinColumn(name = "song_id")
    private SongEntity song;

    @Column(name = "liked_at")
    private LocalDateTime likedAt;


    public SongLikesEntity(UserEntity user, SongEntity song) {
        this.user = user;
        this.song = song;
        this.id = new SongLikesId(user.getId(), song.getId());
        this.likedAt = LocalDateTime.now();
    }


    public SongLikesEntity() {

    }
}