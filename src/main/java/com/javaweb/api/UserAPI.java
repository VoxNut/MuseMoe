package com.javaweb.api;

import com.javaweb.model.dto.MyUserDetail;
import com.javaweb.model.dto.UserDTO;
import com.javaweb.model.request.UserRequestDTO;
import com.javaweb.service.UserService;
import com.javaweb.utils.SecurityUtils;
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

    @GetMapping("/email")
    public ResponseEntity<UserDTO> findUserByEmail(@RequestParam String email) {
        try {
            UserDTO res = userService.findUserByEmail(email);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }

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

    @PutMapping("/last_login")
    public ResponseEntity<Void> updateLastLoginTime() {
        try {
            userService.updateLastLoginTime();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // New Endpoint for Password Reset
    @PutMapping("/reset_password")
    public ResponseEntity<Boolean> resetPassword(@RequestParam Map<String, Object> params) {
        try {
            boolean isUpdated = userService.resetPassword(Long.valueOf(String.valueOf(params.get("id"))), String.valueOf(params.get("password")));
            return ResponseEntity.ok(isUpdated);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }


    @PostMapping("/register")
    public ResponseEntity<Boolean> registerUser(@RequestBody UserRequestDTO userRequestDTO) {
        try {
            boolean res = userService.saveSignUpUser(userRequestDTO);
            return ResponseEntity.ok(res);
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


}

