package com.javaweb.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "playlist")
@Getter
@Setter
public class Playlist extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cover_image_id")
    private Media coverImage;

    @Enumerated(EnumType.STRING)
    private Visibility visibility = Visibility.PRIVATE;

    @Column(name = "last_updated")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdated;

    @OneToMany(mappedBy = "playlist")
    private Set<PlaylistSong> songs = new HashSet<>();

    public enum Visibility {
        PUBLIC, PRIVATE, SHARED
    }
}