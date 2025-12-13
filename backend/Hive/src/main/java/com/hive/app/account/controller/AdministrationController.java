package com.hive.app.account.controller;

import com.hive.app.account.mapper.UserMapper;
import com.hive.app.account.model.UserDTO;
import com.hive.app.account.model.UserRole;
import com.hive.app.account.model.response.UserCardResponse;
import com.hive.app.account.service.UserService;
import com.hive.config.exceptions.PermissionDeniedException;
import com.hive.config.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdministrationController {
    private final UserService service;
    private final UserMapper mapper;

    private static void isAdmin(UserDTO dto) {
        if (dto.getRole() == UserRole.MEMBER) {
            throw new PermissionDeniedException();
        }
    }

    @GetMapping("/user")
    public ResponseEntity<List<UserCardResponse>> getUsers() {
        UserDTO self = (UserDTO) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        isAdmin(self);
        return ResponseEntity.ok(service.getUsers());
    }

    @PutMapping("/user/{id}")
    public ResponseEntity<List<UserCardResponse>> terminateAccount(@PathVariable Long id,
                                                                   @RequestParam boolean ban) throws UserNotFoundException {
        UserDTO self = (UserDTO) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        isAdmin(self);
        service.banUser(id, ban);
        return ResponseEntity.ok(service.getUsers());
    }


}
