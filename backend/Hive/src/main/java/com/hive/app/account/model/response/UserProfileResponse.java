package com.hive.app.account.model.response;

import com.hive.app.account.model.UserRole;

import java.time.LocalDateTime;
import java.util.List;

public interface UserProfileResponse {
    Long getId();

    String getUsername();

    String getEmail();

    String getFullName();

    String getBiography();

    List<String> getInterests();

    UserRole getRole();

    boolean isActive();

    LocalDateTime getCreatedAt();

    int getUserScore();

    int getTimeBank();

    int getFrozenAmount();
}
