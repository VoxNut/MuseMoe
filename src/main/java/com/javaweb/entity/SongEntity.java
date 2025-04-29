package com.javaweb.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "song")
@Getter
@Setter
public class SongEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "album_id")
    private AlbumEntity album;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(nullable = false)
    private Integer duration;

    @Column(length = 50)
    private String genre;

    @Column(name = "release_date")
    @Temporal(TemporalType.DATE)
    private Date releaseDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "audio_file_id", nullable = false)
    private MediaEntity audioFile;

    @ManyToMany
    @JoinTable(name = "song_artist"
            , joinColumns = @JoinColumn(name = "song_id")
            , inverseJoinColumns = @JoinColumn(name = "artist_id"))
    private Set<ArtistEntity> artists = new HashSet<>();


    @ManyToMany
    @JoinTable(
            name = "song_tags",
            joinColumns = @JoinColumn(name = "song_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<TagEntity> tags = new HashSet<>();

    @OneToOne
    private LyricsEntity lyrics;

    @OneToMany(mappedBy = "song")
    private Set<QueueEntity> queues = new HashSet<>();

    @Column(name = "explicit_content")
    private Integer explicitContent;

    @Column(name = "play_count")
    private Integer playCount;

    @Column(name = "average_rating")
    private Double averageRating;


    public void incrementPlayCount() {
        this.playCount++;
    }


}