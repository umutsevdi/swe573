package com.hive.app.account.controller;

import com.hive.app.account.mapper.UserMapper;
import com.hive.app.account.model.UserDTO;
import com.hive.app.account.model.request.UpdateUserRequest;
import com.hive.app.account.model.response.UserCardResponse;
import com.hive.app.account.model.response.UserProfileResponse;
import com.hive.app.account.service.UserService;
import com.hive.config.UserNotFoundException;
import com.hive.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final JwtUtil jwtUtil;
    private final UserService service;
    private final UserMapper mapper = UserMapper.INSTANCE;

    @GetMapping("/{id}")
    public ResponseEntity<UserProfileResponse> getUserById(@RequestHeader(value = "Authorization") String authHeader,
                                                           @PathVariable Long id) throws UserNotFoundException {
        Long authId = jwtUtil.extractId(jwtUtil.getBearer(authHeader));
        var user = service.findById(id)
                .map(mapper::dtoToUserProfileResponse)
                .orElseThrow(UserNotFoundException::new);
        user.setSelf(id.equals(authId));
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{id}/card")
    public ResponseEntity<UserCardResponse> getUserById(@PathVariable Long id) throws UserNotFoundException {
        var user = service.findById(id)
                .map(mapper::dtoToCard)
                .orElseThrow(UserNotFoundException::new);
        return ResponseEntity.ok(user);
    }

    @GetMapping
    public ResponseEntity<UserDTO> getCurrentUser(@RequestHeader(value = "Authorization") String authHeader) throws UserNotFoundException {
        Long id = jwtUtil.extractId(jwtUtil.getBearer(authHeader));
        return ResponseEntity.ok(service.findById(id)
                .filter(UserDTO::isAccountNonLocked)
                .orElseThrow(UserNotFoundException::new));
    }


    @PutMapping
    public ResponseEntity<UserProfileResponse> updateUser(@RequestHeader(value = "Authorization") String
                                                                  authHeader,
                                                          UpdateUserRequest request) {
        Long id = jwtUtil.extractId(jwtUtil.getBearer(authHeader));
        return ResponseEntity.ok(mapper.dtoToUserProfileResponse(service.updateUser(id, request)));
    }


    @DeleteMapping
    public ResponseEntity<Void> deleteUser(@RequestHeader(value = "Authorization") String authHeader) {
        Long id = jwtUtil.extractId(jwtUtil.getBearer(authHeader));
        service.deactivateUser(id);
        return ResponseEntity.ok().build();
    }
}
