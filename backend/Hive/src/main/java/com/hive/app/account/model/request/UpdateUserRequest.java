package com.hive.app.account.model.request;

import lombok.Data;

import java.util.List;

@Data
public class UpdateUserRequest {
    String bio;
    List<String> interests;
    String profileUrl;

    public static UpdateUserRequest profileUrlRequest(String profileUrl) {
        var req = new UpdateUserRequest();
        req.setProfileUrl(profileUrl);
        return req;
    }
}
