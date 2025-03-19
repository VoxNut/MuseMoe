package com.javaweb.api;

import com.javaweb.model.dto.MyUserDetail;
import com.javaweb.model.dto.UserDTO;
import com.javaweb.model.request.UserRequestDTO;
import com.javaweb.service.UserService;
import com.javaweb.utils.SecurityUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserAPI {

    private final UserService userService;

    @GetMapping
    public Set<UserDTO> findAllUser(@RequestParam Map<String, Object> params, @RequestParam(value = "roles", required = false) Set<String> roles) {
        return userService.findAllUser(params, roles);
    }
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser() {
        MyUserDetail principal = SecurityUtils.getPrincipal();

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String username = principal.getUsername();
        UserDTO userDTO = userService.findOneByUsername(username);

        if (userDTO == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.ok(userDTO);
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<UserDTO> getUserByName(@PathVariable String username) {
        UserDTO userDTO = userService.findOneByUsername(username);
        if (userDTO == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(userDTO);
    }

    @GetMapping("{id}/user_entity")
    public UserDTO findUserEntity(@PathVariable("id") Long id) {
        return userService.findUserById(id);
    }



    @GetMapping("username/{username}/user_dto")
    public UserDTO findUserDTOByName(@PathVariable("username") String username) {
        return userService.findUserByUsername(username);
    }

    @PutMapping
    public ResponseEntity<Void> updateUser(@RequestBody UserRequestDTO userRequestDTO) {
        try {
            userService.updateUser(userRequestDTO);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // New Endpoint for Password Reset
    @PutMapping("/{id}/reset-password")
    public ResponseEntity<Void> resetPassword(@PathVariable Long id, @RequestBody PasswordResetRequest request) {
        boolean isUpdated = userService.resetPassword(id, request.getNewPassword());
        if (isUpdated) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @PostMapping("/register")
    public ResponseEntity<Void> registerUser(@RequestBody UserRequestDTO userRequestDTO) {
        try {
            userService.saveSignUpUser(userRequestDTO);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping
    public ResponseEntity<Void> saveUser(@RequestBody UserRequestDTO userRequestDTO) {
        try {
            userService.saveUser(userRequestDTO);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping
    public void deleteUser(@RequestBody Set<Long> ids) {
        userService.delete(ids);
    }

    // DTO for Password Reset Request
    @Getter
    public static class PasswordResetRequest {
        private String newPassword;

    }
}
