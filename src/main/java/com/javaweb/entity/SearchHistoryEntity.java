package com.javaweb.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "search_history")
@Getter
@Setter
public class SearchHistoryEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "search_term", nullable = false)
    private String searchTerm;

    @Column(name = "searched_at")
    private LocalDateTime searchedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "song_id")
    private SongEntity song;

    public SearchHistoryEntity() {
    }

    public SearchHistoryEntity(UserEntity user, SongEntity song, String searchTerm) {
        this.user = user;
        this.song = song;
        this.searchTerm = searchTerm;
        this.searchedAt = LocalDateTime.now();
    }
}