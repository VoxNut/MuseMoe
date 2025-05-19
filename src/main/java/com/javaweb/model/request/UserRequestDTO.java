package com.javaweb.model.request;

import com.javaweb.enums.RoleType;
import com.javaweb.model.dto.AbstractDTO;
import com.javaweb.model.dto.RoleDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.Set;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserRequestDTO extends AbstractDTO {
    private Long id;
    private String username;
    private String password;
    private String email;
    private String fullName;
    private String phone;
    private Integer status;
    private Date createdDate;
    private Date modifiedDate;
    private String createdBy;
    private String modifiedBy;
    private String role;
    private Set<RoleDTO> roles;
    private Set<String> requestRoles;
    private String googleDriveFileId;
    private MultipartFile userAvatar;
    private RoleType roleType;


}