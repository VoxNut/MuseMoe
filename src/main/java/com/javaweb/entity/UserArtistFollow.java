package com.javaweb.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_artist_follow")
@Getter
@Setter
public class UserArtistFollow extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "follower_id")
    private User follower;

    @ManyToOne
    @JoinColumn(name = "artist_id")
    private User artist;

    @Column(name = "followed_at")
    private LocalDateTime followedAt;
}