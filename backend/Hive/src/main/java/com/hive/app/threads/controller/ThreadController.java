package com.hive.app.threads.controller;

import com.hive.app.threads.model.ThreadDTO;
import com.hive.app.threads.model.ThreadRequest;
import com.hive.app.threads.service.ThreadService;
import com.hive.config.NotFoundException;
import com.hive.config.UserNotFoundException;
import com.hive.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/forum")
public class ThreadController {
    private final ThreadService service;
    private final JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<ThreadDTO> newForum(
            @RequestHeader(value = "Authorization") String authHeader,
            @RequestBody ThreadRequest request) throws UserNotFoundException {
        return ResponseEntity.ok(service.newForum(request, jwtUtil.extractId(authHeader)));
    }

    @GetMapping("/{id}")
    ResponseEntity<ThreadDTO> getForum(@PathVariable Long id) throws NotFoundException {
        return ResponseEntity.ok(service.getThread(id));
    }
}
