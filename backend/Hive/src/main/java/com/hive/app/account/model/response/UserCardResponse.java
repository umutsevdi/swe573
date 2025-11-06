package com.hive.app.account.model.response;

import lombok.Data;

import java.util.List;

@Data
public class UserCardResponse {
    private Long id;
    private String fullName;
    private String bio;
    private List<String> interests;
    private int userScore;
}

