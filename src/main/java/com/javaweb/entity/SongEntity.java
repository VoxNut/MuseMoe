package com.javaweb.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

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

    @Column(name = "release_year")
    private Integer releaseYear;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
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
    private Set<TagEntity> tags;

    @OneToOne(mappedBy = "song", cascade = CascadeType.ALL, orphanRemoval = true)
    private LyricsEntity lyrics;

    @OneToMany(mappedBy = "song")
    private Set<QueueEntity> queues = new HashSet<>();

    @Column(name = "explicit_content")
    private Integer explicitContent;

    @Column(name = "play_count")
    private Integer playCount;

    @Column(name = "average_rating")
    private Double averageRating;


    @Builder(toBuilder = true)
    public SongEntity(AlbumEntity album, String title, Integer duration,
                      Integer releaseYear, MediaEntity audioFile, Set<ArtistEntity> artists,
                      Set<TagEntity> tags, LyricsEntity lyrics) {
        this.album = album;
        this.title = title;
        this.duration = duration;
        this.releaseYear = releaseYear;
        this.audioFile = audioFile;
        this.artists = artists != null ? artists : new HashSet<>();
        this.tags = tags != null ? tags : new HashSet<>();
        this.lyrics = lyrics;
        this.playCount = 0;
        this.explicitContent = 0;
    }

    public SongEntity() {
        this.playCount = 0;
        this.explicitContent = 0;
        this.artists = new HashSet<>();
        this.tags = new HashSet<>();
    }

    public synchronized void incrementPlayCount() {
        this.playCount = (this.playCount == null) ? 1 : this.playCount + 1;
    }

    public void addArtist(ArtistEntity artist) {
        if (artist != null) {
            this.artists.add(artist);
            artist.getSongs().add(this);
        }
    }

    public void addTag(TagEntity tag) {
        if (tag != null) {
            this.tags.add(tag);
            tag.getSongs().add(this);
        }
    }

    public void setLyrics(LyricsEntity lyrics) {
        if (lyrics != null) {
            lyrics.setSong(this);
        }
        this.lyrics = lyrics;
    }

}