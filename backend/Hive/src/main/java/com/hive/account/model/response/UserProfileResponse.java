package com.hive.account.model.response;

import com.hive.account.model.UserRole;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserProfileResponse {
    private String username;
    private String email;
    private String fullName;
    private String profileUrl;
    private String biography;
    private List<String> interests;
    private UserRole role;
    private boolean isActive;
    private LocalDateTime createDate;
    private int userScore;
    private boolean isSelf;
    private int remainingTime;
}
