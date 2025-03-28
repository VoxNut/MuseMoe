package com.javaweb.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

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