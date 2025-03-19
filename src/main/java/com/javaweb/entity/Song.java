package com.javaweb.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "song")
@Getter
@Setter
public class Song extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id")
    private Album album;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false)
    private Integer duration;

    @Column(length = 50)
    private String genre;

    @Column(name = "release_date")
    @Temporal(TemporalType.DATE)
    private Date releaseDate;
///
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "audio_file_id", nullable = false)
    private Media audioFile;

    @ManyToMany
    @JoinTable(name = "song_artist"
            , joinColumns = @JoinColumn(name = "song_id")
            , inverseJoinColumns = @JoinColumn(name = "artist_id"))
    private Set<Artist> artists = new HashSet<>();


    @OneToMany(mappedBy = "song")
    private Set<SongLikes> likedUsers = new HashSet<>();

    @ManyToMany(mappedBy = "playedSongs")
    private Set<User> playedByUsers = new HashSet<>();


    @ManyToMany
    @JoinTable(
            name = "song_tags",
            joinColumns = @JoinColumn(name = "song_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    @OneToMany(mappedBy = "song")
    private Set<SongRecommendations>  recommendedUsers = new HashSet<>();

    @OneToMany(mappedBy = "song")
    private Set<PlaylistSong> playlists = new HashSet<>();

    @OneToOne
    private Lyrics lyrics;

    @OneToMany(mappedBy = "song")
    private Set<Queue> queues = new HashSet<>();

    @Column(name = "explicit_content")
    private Integer explicitContent;

    @Column(name = "play_count")
    private Integer playCount;

    @Column(name = "average_rating")
    private Double averageRating;
}