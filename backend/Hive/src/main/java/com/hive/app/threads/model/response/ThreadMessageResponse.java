package com.hive.app.threads.model.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ThreadMessageResponse {
    private String message;
    private LocalDateTime createdAt;
    private Long ownerId;
}
