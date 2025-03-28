package com.javaweb.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_artist_follow")
@Getter
@Setter
public class UserArtistFollowEntity extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "follower_id")
    private UserEntity follower;

    @ManyToOne
    @JoinColumn(name = "artist_id")
    private UserEntity artist;

    @Column(name = "followed_at")
    private LocalDateTime followedAt;
}