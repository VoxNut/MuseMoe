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


}
