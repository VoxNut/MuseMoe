package com.javaweb.entity;

import com.javaweb.enums.AccountStatus;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "user")
@Getter
@Setter
public class User extends BaseEntity {

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(name = "full_name")
    private String fullName;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "avatar_id")
    private Media avatar;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;


    @Enumerated(EnumType.STRING)
    @Column(name = "account_status")
    private AccountStatus accountStatus;

    @Column(name = "preferred_language")
    private String preferredLanguage;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_role"
            , joinColumns = @JoinColumn(name = "user_id")
            , inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Playlist> playlists = new HashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<SongLikes> likedSongs = new HashSet<>();



    @ManyToMany
    @JoinTable(name = "play_history", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "song_id"))
    private Set<Song> playedSongs = new HashSet<>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Artist artist;

    @OneToMany(mappedBy = "user")
    private Set<SongRecommendations> songRecommendations = new HashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<Queue> queues = new HashSet<>();

    @OneToMany(mappedBy = "follower")
    private Set<UserArtistFollow> followers = new HashSet<>();

    @OneToMany(mappedBy = "artist")
    private Set<UserArtistFollow> artists = new HashSet<>();







}