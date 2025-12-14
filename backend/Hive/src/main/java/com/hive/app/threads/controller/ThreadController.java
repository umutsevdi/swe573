package com.hive.app.threads.controller;

import com.hive.app.account.model.UserDTO;
import com.hive.app.threads.model.ThreadDTO;
import com.hive.app.threads.model.ThreadRequest;
import com.hive.app.threads.model.request.ReplyRequest;
import com.hive.app.threads.model.response.ThreadMessageResponse;
import com.hive.app.threads.model.response.ThreadResponse;
import com.hive.app.threads.service.ThreadService;
import com.hive.config.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/thread")
public class ThreadController {
    private final ThreadService service;

    @PostMapping
    public ResponseEntity<ThreadDTO> newThread(
            @RequestBody ThreadRequest request) {
        UserDTO self = (UserDTO) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(service.newForum(request, self));
    }

    @GetMapping
    ResponseEntity<List<ThreadResponse>> getAllThreads() {
        return ResponseEntity.ok(service.getThreads());
    }

    @GetMapping("/{id}")
    ResponseEntity<ThreadDTO> getThread(@PathVariable Long id) throws NotFoundException {
        return ResponseEntity.ok(service.getThread(id));
    }

    @PostMapping("/{id}/reply")
    ResponseEntity<ThreadMessageResponse> reply(
            @PathVariable Long id,
            @RequestBody ReplyRequest message) throws NotFoundException {
        if (message.getMessage() == null || message.getMessage().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        UserDTO self = (UserDTO) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(service.newReply(self, id, message.getMessage()));
    }

    @PutMapping("/{id}")
    ResponseEntity<ThreadDTO> updateThread(
            @PathVariable Long id,
            @RequestBody ThreadRequest request) throws NotFoundException {
        UserDTO self = (UserDTO) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(service.updateThread(self, id, request));
    }

    @PutMapping("/{id}/close")
    ResponseEntity<Void> closeThread(
            @PathVariable Long id) throws NotFoundException {
        UserDTO self = (UserDTO) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        service.closeThread(id, self.getId());
        return ResponseEntity.ok().build();
    }
}
