package com.hive.account.model.request;

import lombok.Data;

import java.util.List;

@Data
public class UpdateUserRequest {
    String bio;
    List<String> interests;
    String profileUrl;
}
