package com.hive.app.threads.model;

import com.hive.app.threads.model.response.ThreadMessageResponse;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class ThreadDTO {
    String description;
    private Long id;
    private String title;
    private List<String> tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long ownerId;
    private boolean active;
    private List<ThreadMessageResponse> messages = new ArrayList<>();
}
