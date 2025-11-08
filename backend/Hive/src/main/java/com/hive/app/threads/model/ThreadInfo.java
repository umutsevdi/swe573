package com.hive.app.threads.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.jsonwebtoken.lang.Strings;

import java.time.LocalDateTime;
import java.util.List;

public interface ThreadInfo {
    Long getId();

    Long getOwnerId();

    String getTitle();

    String getDescription();

    LocalDateTime getCreatedAt();

    @JsonIgnore
    String getTags();

    default List<String> getTagsList() {
        if (getTags().contains(",")) {
            return List.of(Strings.split(getTags(), ","));
        }
        return List.of(getTags());
    }

    boolean isActive();
}