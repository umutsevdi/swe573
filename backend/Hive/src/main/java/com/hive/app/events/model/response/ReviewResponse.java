package com.hive.app.events.model.response;

import com.hive.app.events.model.DeliveryStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewResponse {
    private Long id;
    private Long eventId;
    private Long userId;
    private String username;
    private Integer rating;
    private DeliveryStatus deliveryStatus;
    private String message;
    private LocalDateTime createdAt;
}