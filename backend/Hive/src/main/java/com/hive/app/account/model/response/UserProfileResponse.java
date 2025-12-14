package com.hive.app.account.model.response;

import com.hive.app.account.model.UserRole;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserProfileResponse {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String biography;
    private List<String> interests;
    private UserRole role;
    private boolean active;
    private boolean suspended;
    private LocalDateTime createdAt;
    private int userScore;
    private int timeBank;
    private int frozenAmount;
}
