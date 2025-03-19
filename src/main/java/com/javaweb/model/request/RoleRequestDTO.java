package com.javaweb.model.request;

import com.javaweb.model.dto.AbstractDTO;

public class RoleRequestDTO extends AbstractDTO<RoleRequestDTO> {
    private static final long serialVersionUID = 5830885581031027382L;

    private String name;
    private String description;
    private String code;
}
