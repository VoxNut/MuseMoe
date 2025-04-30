package com.javaweb.entity;

import jakarta.persistence.*;
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

    @Column(name = "tag_type")
    @Enumerated(EnumType.STRING)
    private TagType tagtype;

    @Column(name = "description")
    private String description;


    @ManyToMany(mappedBy = "tags")
    private Set<SongEntity> songs;


    public enum TagType {
        GENRE, MOOD, INSTRUMENT, THEME, TEMPO, ATMOSPHERE, CONTEXT, ERA, LANGUAGE
    }
}
