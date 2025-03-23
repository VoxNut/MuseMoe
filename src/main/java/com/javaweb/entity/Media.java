package com.javaweb.entity;

import com.javaweb.enums.MediaType;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "media")
@Getter
@Setter
public class Media extends BaseEntity {

    @Column(name = "file_url", nullable = false)
    private String fileUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", nullable = false)
    private MediaType fileType;

    @Column(name = "file_size")
    private Integer fileSize;




}