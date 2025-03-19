package com.javaweb.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
public class AbstractDTO<T> implements Serializable {
    private static final long serialVersionUID = 7213600440729202783L;

    private Long id;
    private Date createdDate;
    private String createdBy;
    private Date modifiedDate;
    private String modifiedBy;
}