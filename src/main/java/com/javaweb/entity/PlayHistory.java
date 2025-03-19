package com.javaweb.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "play_history")
@Getter
@Setter
public class PlayHistory extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "song_id")
    private Song song;

    @Column(name = "played_at")
    private LocalDateTime playedAt;
}