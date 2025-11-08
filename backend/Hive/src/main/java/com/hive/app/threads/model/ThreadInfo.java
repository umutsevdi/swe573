package com.hive.app.threads.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.jsonwebtoken.lang.Strings;

import java.time.LocalDateTime;
import java.util.List;

public interface ThreadInfo {
    Long getId();

    String getTitle();

    LocalDateTime getCreatedAt();

    @JsonIgnore
    String getTags();

    default List<String> getTagsList() {
        return List.of(Strings.split(getTags(), ","));
    }

    boolean isActive();
}