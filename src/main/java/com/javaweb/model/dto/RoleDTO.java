package com.javaweb.model.dto;

import com.javaweb.enums.RoleType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleDTO extends AbstractDTO<RoleDTO> {
    private String name;
    private String description;
    private RoleType code;


}