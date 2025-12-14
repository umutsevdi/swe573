package com.hive.app.account.repository;

import com.hive.app.account.model.UserRole;

import java.util.ArrayList;
import java.util.List;

public interface UserCardView {
    Long getId();

    String getFullName();

    String getBiography();

    int getUserScore();

    String getInterestsString();

    boolean isSuspended();

    UserRole getRole();

    default List<String> getInterests() {
        if (getInterestsString() == null || getInterestsString().isEmpty()) {
            return new ArrayList<>();
        }
        return List.of(getInterestsString().split(","));
    }

}

