package com.hive.app.threads.model.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ThreadResponse {
    private Long id;
    private String description;
    private String title;
    private LocalDateTime createdAt;
    private Long ownerId;
    private boolean active;
    private List<String> tags;

}
