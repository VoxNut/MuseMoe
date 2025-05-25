package com.javaweb.api;

import com.javaweb.enums.AccountStatus;
import com.javaweb.enums.RoleType;
import com.javaweb.model.dto.MyUserDetail;
import com.javaweb.model.dto.UserDTO;
import com.javaweb.model.request.UserRequestDTO;
import com.javaweb.service.UserService;
import com.javaweb.utils.DateUtil;
import com.javaweb.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
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

    @GetMapping("/check_user_artist")
    public ResponseEntity<Boolean> checkUserArtist(@RequestParam Long currentArtistId) {
        try {
            boolean res = userService.checkUserArtist(currentArtistId);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
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
    public ResponseEntity<Void> updateLastLoginTime(@RequestBody UserDTO userDTO) {
        try {
            userService.updateLastLoginTime(userDTO.getLastLoginDate());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/reset_password")
    public ResponseEntity<Boolean> resetPassword(@RequestBody UserDTO userDTO) {
        try {
            boolean isUpdated = userService.resetPassword(userDTO.getId(), userDTO.getPassword());
            return ResponseEntity.ok(isUpdated);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }


    @PostMapping("/register")
    public ResponseEntity<Boolean> registerUser(@ModelAttribute UserRequestDTO userRequestDTO) {
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

    @PutMapping("/upgrade")
    public ResponseEntity<Boolean> upgradeUser(@RequestBody UserRequestDTO userRequestDTO) {
        try {
            boolean res = userService.upgradeUser(userRequestDTO);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<UserDTO>> findAll(@RequestParam AccountStatus accountStatus) {
        try {
            List<UserDTO> res = userService.findAll(accountStatus);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/filter")
    public ResponseEntity<List<UserDTO>> fetchUsersByFilter(

            @RequestParam(value = "from", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date from,
            @RequestParam(value = "to", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date to,

            @RequestParam(value = "roleType", required = false) RoleType roleType
    ) {
        List<UserDTO> results = userService.findFilteredUsers(DateUtil.toLocalDateTime(from), DateUtil.toLocalDateTime(to), roleType);
        return ResponseEntity.ok(results);
    }


}

