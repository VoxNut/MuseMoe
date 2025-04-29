package com.javaweb.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;


@Entity
@Table(name = "lyrics")
@Getter
@Setter
public class LyricsEntity extends BaseEntity {

    @OneToOne
    @JoinColumn(name = "song_id")
    private SongEntity song;

    private String content;
}