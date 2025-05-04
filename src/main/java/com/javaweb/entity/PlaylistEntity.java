package com.javaweb.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "playlist")
@Getter
@Setter
public class PlaylistEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cover_image_id")
    private StreamingMediaEntity coverImage;

    @Enumerated(EnumType.STRING)
    private Visibility visibility = Visibility.PRIVATE;

    @Column(name = "last_updated")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdated;


    @Column(name = "total_duration")
    private Integer totalDuration;

    @OneToMany(mappedBy = "playlist"
            , cascade = CascadeType.ALL
            , orphanRemoval = true
            , fetch = FetchType.EAGER)
    private List<PlaylistSongEntity> playlistSongEntities;


    public enum Visibility {
        PUBLIC, PRIVATE, SHARED
    }


}