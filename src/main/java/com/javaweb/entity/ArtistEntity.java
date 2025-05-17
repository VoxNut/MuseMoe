package com.javaweb.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "artist")
@Getter
@Setter
public class ArtistEntity extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private UserEntity user;

    @Column(name = "stage_name")
    private String stageName;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "profile_pic_id")
    private StreamingMediaEntity profilePic;

    @OneToMany(mappedBy = "artist", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<AlbumEntity> albums = new HashSet<>();

    @ManyToMany(mappedBy = "artists")
    private Set<SongEntity> songs = new HashSet<>();


    @OneToMany(mappedBy = "artist")
    private Set<UserArtistFollowEntity> followers;


}