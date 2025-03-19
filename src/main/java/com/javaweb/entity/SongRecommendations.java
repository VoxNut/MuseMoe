package com.javaweb.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "song_recommendations")
@Getter
@Setter
public class SongRecommendations extends BaseEntity {


    @ManyToOne
    @JoinColumn(name = "song_id")
    private Song song;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "recommendation_strength")
    private Double recommendationStrength;

}
