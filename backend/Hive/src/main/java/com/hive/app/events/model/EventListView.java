package com.hive.app.events.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.jsonwebtoken.lang.Strings;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface EventListView {
    Long getId();

    String getTitle();

    @JsonIgnore
    String getTags();

    Integer getMaxParticipants();

    EventType getType();

    Integer getDuration();

    LocalDate getEventDate();

    LocalDateTime getCreatedAt();

    LocalDateTime getUpdatedAt();

    Long getOwnerId();

    @JsonIgnore
    Double getLatitude();

    @JsonIgnore
    Double getLongitude();

    @JsonIgnore
    String getLocationName();


    default List<String> getTagsList() {
        if (getTags().contains(",")) {
            return List.of(Strings.split(getTags(), ","));
        }
        return List.of(getTags());
    }

    default Location getLocation() {
        return new Location(
                getLatitude(),
                getLongitude(),
                getLocationName()
        );
    }
}
