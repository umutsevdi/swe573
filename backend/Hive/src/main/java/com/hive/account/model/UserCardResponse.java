package com.hive.account.model;

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

