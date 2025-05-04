package com.javaweb.model.dto;

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
public class PasswordDTO extends AbstractDTO {

    private static final long serialVersionUID = 8835146939192307340L;

    private String oldPassword;
    private String newPassword;
    private String confirmPassword;

}