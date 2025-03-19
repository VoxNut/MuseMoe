package com.javaweb.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "queue")
@Getter
@Setter
public class Queue extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "song_id")
    private Song song;

    @Column(name = "position")
    private Integer position;

    @Column(name = "added_at")
    private LocalDateTime addedAt;
}