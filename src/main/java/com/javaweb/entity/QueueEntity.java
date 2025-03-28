package com.javaweb.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

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
    
}