package com.hive.app.account.model.response;

import com.hive.app.account.model.UserRole;
import lombok.Data;

import java.util.List;

@Data
public class UserCardResponse {
    private Long id;
    private String fullName;
    private String biography;
    private int userScore;
    private List<String> interests;
    private boolean suspended;
    private UserRole role;
}

