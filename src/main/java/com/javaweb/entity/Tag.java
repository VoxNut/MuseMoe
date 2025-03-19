package com.javaweb.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.Set;

@Entity
@Table(name = "tag")
@Getter
@Setter
public class Tag extends BaseEntity {

    @Column(name ="name")
    private String name;

    @Column(name ="description")
    private String description;


    @ManyToMany(mappedBy = "tags")
    private Set<Song> songs;
}
