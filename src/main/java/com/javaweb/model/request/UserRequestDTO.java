package com.javaweb.model.request;

import com.javaweb.model.dto.AbstractDTO;
import com.javaweb.model.dto.RoleDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Set;

@Getter
@Setter
public class UserRequestDTO extends AbstractDTO<UserRequestDTO> {
    private Long id;
    private String username;
    private String password;
    private String email;
    private String fullName;
    private String phone;
    private Integer status;
    private String avatar;
    private Date createdDate;
    private Date modifiedDate;
    private String createdBy;
    private String modifiedBy;
    private String role;
    private Set<RoleDTO> roles;
    private Set<String> requestRoles;


}