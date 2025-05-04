package com.javaweb.model.request;

import com.javaweb.model.dto.AbstractDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class RoleRequestDTO extends AbstractDTO {
    private static final long serialVersionUID = 5830885581031027382L;

    private String name;
    private String description;
    private String code;
}
