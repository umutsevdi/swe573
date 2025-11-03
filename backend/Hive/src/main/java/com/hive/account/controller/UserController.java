package com.hive.account.controller;

import com.hive.account.mapper.UserMapper;
import com.hive.account.model.UserDTO;
import com.hive.account.model.request.UpdateUserRequest;
import com.hive.account.model.response.UserProfileResponse;
import com.hive.account.service.UserService;
import com.hive.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final JwtUtil jwtUtil;
    private final UserService service;
    private final UserMapper mapper = UserMapper.INSTANCE;

    @GetMapping("/{id}")
    public ResponseEntity<UserProfileResponse> getUserById(@RequestHeader(value = "Authorization") String authHeader,
                                                           @PathVariable Long id) {
        String username = getUsername(authHeader);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        var user = service.findById(id)
                .map(mapper::dtoToUserProfileResponse)
                .orElseThrow(() -> new UsernameNotFoundException(id + " not found!"));
        user.setSelf(username.equals(user.getUsername()));
        return ResponseEntity.ok(user);
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(@RequestHeader(value = "Authorization") String authHeader) {
        String username = getUsername(authHeader);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(service.findByUsername(username)
                .filter(UserDTO::isAccountNonLocked)
                .orElseThrow(() -> new UsernameNotFoundException(username + " not found!")));
    }


    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateUser(@RequestHeader(value = "Authorization") String authHeader,
                                                          UpdateUserRequest request) {
        String username = getUsername(authHeader);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(mapper.dtoToUserProfileResponse(service.updateUser(username, request)));
    }


    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteUser(@RequestHeader(value = "Authorization") String authHeader) {
        String username = getUsername(authHeader);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        service.deactivateUser(username);
        return ResponseEntity.ok().build();
    }

    private String getUsername(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return jwtUtil.extractUsername(authHeader.substring(7));
    }

}
