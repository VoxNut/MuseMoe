package com.javaweb.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Entity
@Table(name = "queue")
@Getter
@Setter
public class QueueEntity extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "song_id")
    private SongEntity song;

    @Column(name = "position")
    private Integer position;


    public QueueEntity() {

    }

    public QueueEntity(UserEntity user, SongEntity song, Integer nextPosition) {
        this.user = user;
        this.song = song;
        this.position = nextPosition;
    }


}