package com.hive.app.events.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.jsonwebtoken.lang.Strings;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface EventListEntityView {
    Long getId();

    String getTitle();

    @JsonIgnore
    String getTags();

    Location getLocation();

    Integer getMaxParticipants();

    EventType getType();

    Integer getDuration();

    LocalDate getEventDate();

    LocalDateTime getCreatedAt();

    LocalDateTime getUpdatedAt();

    Long getOwnerId();

    default List<String> getTagsList() {
        if (getTags().contains(",")) {
            return List.of(Strings.split(getTags(), ","));
        }
        return List.of(getTags());
    }
}
