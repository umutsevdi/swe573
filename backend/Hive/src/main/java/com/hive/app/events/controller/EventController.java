package com.hive.app.events.controller;

import com.hive.app.account.model.UserDTO;
import com.hive.app.events.model.EventListView;
import com.hive.app.events.model.EventRequest;
import com.hive.app.events.model.EventResponse;
import com.hive.app.events.service.EventService;
import com.hive.config.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/event")
public class EventController {
    private final EventService service;

    @PostMapping
    public ResponseEntity<EventResponse> newEvent(
            @RequestBody EventRequest request) {
        UserDTO self = (UserDTO) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(service.newEvent(request, self));
    }

    @GetMapping("/owner/{id}")
    ResponseEntity<List<EventListView>> getUserEvents(@PathVariable Long id, @RequestParam(value = "all", required =
            false, defaultValue = "false") boolean all) {
        return ResponseEntity.ok(service.getUserEvents(id, all));
    }

    @GetMapping("/{id}")
    ResponseEntity<EventResponse> getEvent(@PathVariable Long id) throws NotFoundException {
        return ResponseEntity.ok(service.getEvent(id));
    }

    @PutMapping("/{id}")
    ResponseEntity<EventResponse> updateEvent(
            @PathVariable Long id,
            @RequestBody EventRequest request) throws NotFoundException {
        UserDTO self = (UserDTO) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(service.updateEvent(id, request, self));
    }
}
