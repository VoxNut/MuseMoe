package com.javaweb.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@Setter
public abstract class BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 7213600440729202783L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name = "created_at", updatable = false)
    private LocalDateTime created_at;

    @Column(name = "updated_at")
    private LocalDateTime updated_at;

    @PrePersist
    protected void onCreate() {
        created_at = LocalDateTime.now();
        updated_at = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updated_at = LocalDateTime.now();
    }
}