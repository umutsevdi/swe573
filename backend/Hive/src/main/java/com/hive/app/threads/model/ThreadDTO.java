package com.hive.app.threads.model;

import com.hive.app.account.model.response.UserCardResponse;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class ThreadDTO {
    private Long id;
    private String title;
    private List<String> tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserCardResponse owner;
    private boolean active;
    private List<ThreadMessageDTO> messages = new ArrayList<>();
}
