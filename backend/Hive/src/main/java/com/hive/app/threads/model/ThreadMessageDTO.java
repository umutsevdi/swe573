package com.hive.app.threads.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ThreadMessageDTO {
    private String message;
    private LocalDateTime createdAt;
    private Long ownerId;
}
