package com.javaweb.model.dto;

import com.javaweb.enums.RoleType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class RoleDTO extends AbstractDTO {
    private String name;
    private String description;
    private RoleType code;


}