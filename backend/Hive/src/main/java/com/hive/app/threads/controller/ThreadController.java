package com.hive.app.threads.controller;

import com.hive.app.account.model.UserDTO;
import com.hive.app.threads.model.ThreadDTO;
import com.hive.app.threads.model.ThreadInfo;
import com.hive.app.threads.model.ThreadMessageProjection;
import com.hive.app.threads.model.ThreadRequest;
import com.hive.app.threads.service.ThreadService;
import com.hive.config.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/thread")
@Log4j2
public class ThreadController {
    private final ThreadService service;

    @PostMapping
    public ResponseEntity<ThreadDTO> newThread(
            @RequestBody ThreadRequest request) {
        log.info("Received, {}", request);
        UserDTO self = (UserDTO) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(service.newForum(request, self));
    }

    @GetMapping
    ResponseEntity<List<ThreadInfo>> getAllThreads(@RequestParam(value = "show", required = false)
                                                   Boolean show) {
        return ResponseEntity.ok(service.getThreads(show));
    }

    @GetMapping("/{id}")
    ResponseEntity<ThreadDTO> getThread(@PathVariable Long id) throws NotFoundException {
        log.info("Received, {}", id);
        return ResponseEntity.ok(service.getThread(id));
    }

    @PostMapping(
            path = "/{id}/reply",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.TEXT_PLAIN_VALUE
    )
    ResponseEntity<ThreadMessageProjection> reply(
            @PathVariable Long id,
            @RequestBody String message) throws NotFoundException {
        log.info("Received , {} {}", id, message);
        if (message == null || message.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        UserDTO self = (UserDTO) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(service.newReply(self, id, message));
    }

    @PutMapping("/{id}")
    ResponseEntity<ThreadDTO> updateThread(
            @PathVariable Long id,
            @RequestBody ThreadRequest request) throws NotFoundException {
        UserDTO self = (UserDTO) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("Received , {} {}", id, request);
        return ResponseEntity.ok(service.updateThread(self, id, request));
    }

    @GetMapping("/{id}/close")
    ResponseEntity<Void> closeThread(
            @PathVariable Long id) throws NotFoundException {
        UserDTO self = (UserDTO) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        service.closeThread(id, self.getId());
        return ResponseEntity.accepted().build();
    }
}
