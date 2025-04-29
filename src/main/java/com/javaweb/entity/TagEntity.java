package com.javaweb.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Entity
@Table(name = "tag")
@Getter
@Setter
public class TagEntity extends BaseEntity {

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;


    @ManyToMany(mappedBy = "tags")
    private Set<SongEntity> songs;
}
