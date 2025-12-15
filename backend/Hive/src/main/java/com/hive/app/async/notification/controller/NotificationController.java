package com.hive.app.async.notification.controller;

import com.hive.app.account.model.UserDTO;
import com.hive.app.async.notification.model.NotificationEntity;
import com.hive.app.async.notification.model.response.NotificationResponse;
import com.hive.app.async.notification.service.NotificationService;
import com.hive.app.account.mapper.UserMapper;
import com.hive.app.async.notification.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/inbox")
@RequiredArgsConstructor
@Log4j2
public class NotificationController {
    private final NotificationService notificationService;
    private final UserMapper userMapper;
    private final NotificationMapper notificationMapper;

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getUserNotifications() {
        UserDTO self = (UserDTO) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(notificationService.getUserNotifications(self));
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getUnreadCount() {
        UserDTO self = (UserDTO) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        long count = notificationService.getUnreadCount(self);
        return ResponseEntity.ok(count);
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long notificationId) {
        UserDTO self = (UserDTO) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        int updated = notificationService.markAsRead(notificationId, self.getId());
        if (updated == 0) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok().build();
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        UserDTO self = (UserDTO) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        notificationService.markAllAsRead(self.getId());
        return ResponseEntity.ok().build();
    }
}
