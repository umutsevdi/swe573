package com.hive.app.account.model.request;

import lombok.Data;

import java.util.List;

@Data
public class UpdateUserRequest {
    private String bio;
    private List<String> interests;
}
