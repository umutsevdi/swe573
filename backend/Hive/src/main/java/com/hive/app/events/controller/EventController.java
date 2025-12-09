package com.hive.app.events.controller;

import com.hive.app.account.model.UserDTO;
import com.hive.app.events.model.*;
import com.hive.app.events.service.EventService;
import com.hive.config.exceptions.LogicException;
import com.hive.config.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/event")
@Log4j2
public class EventController {
    private final EventService service;

    @PostMapping
    public ResponseEntity<EventResponse> newEvent(
            @RequestBody EventRequest request) {
        UserDTO self = (UserDTO) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(service.newEvent(request, self));
    }

    @PostMapping("/search")
    public ResponseEntity<List<EventResponse>> getAllEventsBy(@RequestBody EventQueryOptions options) {
        log.info("getAllEventsBy {}", options);
        return ResponseEntity.ok(service.getAllEventsBy(options));
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
            @RequestBody EventRequest request) throws NotFoundException, LogicException {
        UserDTO self = (UserDTO) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(service.updateEvent(id, request, self));
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> cancel(
            @PathVariable Long id) throws NotFoundException, LogicException {
        service.cancelEvent(id, (UserDTO) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/participation")
    ResponseEntity<Void> participate(@PathVariable Long id, @RequestParam String message) throws NotFoundException, LogicException {
        UserDTO self = (UserDTO) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        service.participate(id, self, message);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/participation")
    ResponseEntity<Void> withdraw(@PathVariable Long id) throws NotFoundException,
            LogicException {
        UserDTO self = (UserDTO) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        service.withdraw(id, self);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/participation")
    ResponseEntity<Void> manageParticipation(@PathVariable Long id,
                                             @RequestBody ParticipationAssignmentRequest request) throws NotFoundException,
            LogicException {
        UserDTO self = (UserDTO) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        service.assign(id, self, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/participation")
    ResponseEntity<Map<Long, ParticipationState>> getParticipants(@PathVariable Long id) {
        return ResponseEntity.ok(service.getParticipants(id));
    }

}
