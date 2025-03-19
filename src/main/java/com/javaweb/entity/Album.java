package com.javaweb.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "album")
@Getter
@Setter
public class Album extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artist_id", nullable = false)
    private Artist artist;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(name = "release_date")
    @Temporal(TemporalType.DATE)
    private Date releaseDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cover_art_id")
    private Media coverArt;

    @OneToMany(mappedBy = "album", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Song> songs = new HashSet<>();
}