package com.javaweb.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Entity
@Table(name = "song_recommendations")
@Getter
@Setter
public class SongRecommendationsEntity {

    @EmbeddedId
    private SongRecommendationsId id;

    @Column(name = "recommendation_strength")
    private double recommendationStrength;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("songId")
    @JoinColumn(name = "song_id")
    private SongEntity song;

    public SongRecommendationsEntity(UserEntity user, SongEntity song) {
        this.user = user;
        this.song = song;
        this.id = new SongRecommendationsId(user.getId(), song.getId());
    }

    public SongRecommendationsEntity() {
        this.id = new SongRecommendationsId();
    }

    public void setUser(UserEntity user) {
        this.user = user;
        if (this.id == null) this.id = new SongRecommendationsId();
        if (user != null) this.id.setUserId(user.getId());
    }

    public void setSong(SongEntity song) {
        this.song = song;
        if (this.id == null) this.id = new SongRecommendationsId();
        if (song != null) this.id.setSongId(song.getId());
    }
}



