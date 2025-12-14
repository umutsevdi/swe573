package com.hive.app.account.controller;

import com.hive.app.account.model.UserDTO;
import com.hive.app.account.model.UserRole;
import com.hive.app.account.model.response.UserCardResponse;
import com.hive.app.account.service.UserService;
import com.hive.app.async.transaction.model.TransactionRecord;
import com.hive.app.async.transaction.model.request.TransactionRequest;
import com.hive.app.async.transaction.service.TransactionService;
import com.hive.config.exceptions.LogicException;
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
    private final TransactionService transactionService;

    @GetMapping("/users")
    public ResponseEntity<List<UserCardResponse>> getUsers() {
        UserDTO self = (UserDTO) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(service.getUsers());
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> suspend(@PathVariable Long id,
                                        @RequestParam boolean suspend) throws UserNotFoundException, LogicException {
        UserDTO self = (UserDTO) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        service.suspendUser(id, suspend, self);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<Void> setUserRole(@PathVariable Long id,
                                            @RequestParam UserRole role) throws UserNotFoundException, LogicException {
        UserDTO self = (UserDTO) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (self.getRole() != UserRole.OWNER) {
            throw new LogicException("Only the owner can assign roles.");
        } else if (id.equals(self.getId())) {
            throw new LogicException("Can not update owner's role.");
        }
        service.setUserRole(id, role, self);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/transactions")
    public ResponseEntity<List<TransactionRecord>> getTransactions(@RequestBody TransactionRequest request) {
        UserDTO self = (UserDTO) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(transactionService.getAllEventsBy(request));
    }


}
