package com.javaweb.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_artist_follow")
@Getter
@Setter
public class UserArtistFollowEntity implements Serializable {

    @EmbeddedId
    private UserArtistFollowId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("followerId")
    @JoinColumn(name = "follower_id")
    private UserEntity follower;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("artistId")
    @JoinColumn(name = "artist_id")
    private ArtistEntity artist;

    @Column(name = "followed_at")
    private LocalDateTime followedAt;

    public UserArtistFollowEntity() {
    }

    public UserArtistFollowEntity(UserEntity follower, ArtistEntity artist) {
        this.follower = follower;
        this.artist = artist;
        this.id = new UserArtistFollowId(follower.getId(), artist.getId());
        this.followedAt = LocalDateTime.now();
    }
}