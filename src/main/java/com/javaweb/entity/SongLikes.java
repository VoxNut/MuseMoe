package com.javaweb.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "song_likes")
@Getter
@Setter
public class SongLikes extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "song_id")
    private Song song;

    @Column(name = "liked_at")
    private LocalDateTime likedAt;

}