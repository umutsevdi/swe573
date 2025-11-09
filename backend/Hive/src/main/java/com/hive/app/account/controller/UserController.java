package com.hive.app.account.controller;

import com.hive.app.account.mapper.UserMapper;
import com.hive.app.account.model.UserDTO;
import com.hive.app.account.model.request.UpdateUserRequest;
import com.hive.app.account.model.response.UserCardResponse;
import com.hive.app.account.model.response.UserProfileResponse;
import com.hive.app.account.service.UserService;
import com.hive.config.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Log4j2
public class UserController {
    private final UserService service;
    private final UserMapper mapper;

    @GetMapping("/{id}")
    public ResponseEntity<UserProfileResponse> getUserById(@PathVariable Long id) throws UserNotFoundException {
        UserDTO self = (UserDTO) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        var user = mapper.dtoToUserProfileResponse(self.getId().equals(id) ? self :
                service.findById(id).orElseThrow(UserNotFoundException::new)
        );
        user.setSelf(id.equals(self.getId()));
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{id}/card")
    public ResponseEntity<UserCardResponse> getUserByIdCard(@PathVariable Long id) throws UserNotFoundException {
        UserDTO self = (UserDTO) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(mapper.dtoToCard(
                self.getId().equals(id) ? self :
                        service.findById(id).orElseThrow(UserNotFoundException::new)
        ));
    }

    @GetMapping
    public ResponseEntity<UserDTO> getCurrentUser() {
        return ResponseEntity.ok((UserDTO) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }


    @PutMapping
    public ResponseEntity<UserProfileResponse> updateUser(UpdateUserRequest request) {
        UserDTO self = (UserDTO) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(mapper.dtoToUserProfileResponse(service.updateUser(self, request)));
    }


    @DeleteMapping
    public ResponseEntity<Void> deleteUser() {
        service.deactivateUser((UserDTO) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        return ResponseEntity.ok().build();
    }
}
