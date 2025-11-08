package com.hive.app.threads.model;

import com.hive.app.account.model.response.UserCardResponse;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ThreadMessageDTO {
    private String message;
    private LocalDateTime createdAt;
    private UserCardResponse owner;
}
