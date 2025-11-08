package com.hive.app.threads.controller;

import com.hive.app.threads.model.ThreadDTO;
import com.hive.app.threads.model.ThreadInfo;
import com.hive.app.threads.model.ThreadMessageDTO;
import com.hive.app.threads.model.ThreadRequest;
import com.hive.app.threads.service.ThreadService;
import com.hive.config.NotFoundException;
import com.hive.config.UserNotFoundException;
import com.hive.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/forum")
public class ThreadController {
    private final ThreadService service;
    private final JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<ThreadDTO> newThread(
            @RequestHeader(value = "Authorization") String authHeader,
            @RequestBody ThreadRequest request) throws UserNotFoundException {
        return ResponseEntity.ok(service.newForum(request, jwtUtil.extractId(authHeader)));
    }

    @GetMapping()
    ResponseEntity<List<ThreadInfo>> getAllThreads(@RequestParam(value = "show", required = false)
                                                   Boolean show) {
        return ResponseEntity.ok(service.getThreads(show));
    }

    @GetMapping("/{id}")
    ResponseEntity<ThreadDTO> getThread(@PathVariable Long id) throws NotFoundException {
        return ResponseEntity.ok(service.getThread(id));
    }

    @GetMapping("/{id}/reply")
    ResponseEntity<ThreadMessageDTO> reply(
            @RequestHeader(value = "Authorization") String authHeader,
            @PathVariable Long id,
            @RequestParam(value = "msg") String message) throws NotFoundException {
        if (message == null || message.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(service.newReply(id, jwtUtil.extractId(authHeader), message));
    }

    @PutMapping("/{id}")
    ResponseEntity<ThreadDTO> updateThread(
            @RequestHeader(value = "Authorization") String authHeader,
            @PathVariable Long id,
            @RequestBody ThreadRequest request) throws NotFoundException {
        return ResponseEntity.ok(service.updateThread(request, id, jwtUtil.extractId(authHeader)));
    }

    @GetMapping("/{id}/close")
    ResponseEntity<Void> closeThread(
            @RequestHeader(value = "Authorization") String authHeader,
            @PathVariable Long id) throws NotFoundException {
        service.closeThread(id, jwtUtil.extractId(authHeader));
        return ResponseEntity.accepted().build();
    }
}
