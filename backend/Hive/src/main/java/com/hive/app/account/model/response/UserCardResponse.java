package com.hive.app.account.model.response;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;

public interface UserCardResponse {
    Long getId();

    String getFullName();

    String getBiography();

    int getUserScore();

    @JsonIgnore
    String getInterestsString();

    default List<String> getInterests() {
        if (getInterestsString() == null || getInterestsString().isEmpty()) {
            return new ArrayList<>();
        }
        return List.of(getInterestsString().split(","));
    }

}

