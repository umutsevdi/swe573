package com.hive.app.threads.model.response;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public interface ThreadResponse {
    String getDescription();

    Long getId();

    String getTitle();

    LocalDateTime getCreatedAt();

    Long getOwnerId();

    boolean getActive();

    @JsonIgnore
    String getTagsList();

    default List<String> getTags() {
        String tags = getTagsList();
        if (tags == null || tags.isEmpty()) {
            return new ArrayList<>();
        }
        return List.of(tags.split(","));

    }

}
