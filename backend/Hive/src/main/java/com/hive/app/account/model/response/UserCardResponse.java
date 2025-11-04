package com.hive.app.account.model.response;

import lombok.Data;

import java.util.List;

@Data
public class UserCardResponse {
    String profileUrl;
    String fullName;
    String bio;
    List<String> interests;
    int userScore;
}

