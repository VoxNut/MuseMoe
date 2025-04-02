package com.javaweb.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "play_history")
@Getter
@Setter
public class PlayHistoryEntity implements Serializable {

    private static final long serialVersionUID = 7213600440729202783L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "played_at")
    private LocalDateTime playedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "song_id")
    private SongEntity song;

    public PlayHistoryEntity() {
    }

    public PlayHistoryEntity(UserEntity user, SongEntity song) {
        this.user = user;
        this.song = song;
        this.playedAt = LocalDateTime.now();
    }
}