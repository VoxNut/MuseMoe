package com.javaweb.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserArtistFollowId implements Serializable {

    @Column(name = "follower_id")
    private Long followerId;

    @Column(name = "artist_id")
    private Long artistId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserArtistFollowId that = (UserArtistFollowId) o;
        return Objects.equals(followerId, that.followerId) &&
                Objects.equals(artistId, that.artistId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(followerId, artistId);
    }
}