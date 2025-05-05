package com.javaweb.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Entity
@Table(name = "lyrics")
@Getter
@Setter
public class LyricsEntity extends BaseEntity {

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "song_id", nullable = false)
    private SongEntity song;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(length = 10)
    private String language = "en";

    public LyricsEntity(String content) {
        this.content = content;
    }

    public LyricsEntity() {
    }


}