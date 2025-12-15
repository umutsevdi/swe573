package com.hive.app.async.notification.model.response;

import com.hive.app.async.notification.model.NotificationType;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;

@Data
@RequiredArgsConstructor
public class NotificationResponse {
    private Long id;
    private Long ownerId;
    private NotificationType type;
    private String message;
    private boolean isRead;
    private LocalDateTime createdAt;
    private String redirectTo;
}
