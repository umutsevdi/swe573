package com.hive.app.events.model.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hive.app.events.model.EventType;
import com.hive.app.events.model.Location;
import io.jsonwebtoken.lang.Strings;

import java.time.LocalDateTime;
import java.util.List;

public interface EventListResponse {
    Long getId();

    String getTitle();

    @JsonIgnore
    String getTagsString();

    Integer getMaxParticipants();

    EventType getType();

    Integer getDuration();

    LocalDateTime getEventDate();

    LocalDateTime getCreatedAt();

    LocalDateTime getUpdatedAt();

    Long getOwnerId();

    boolean isCancelled();

    @JsonIgnore
    Double getLatitude();

    @JsonIgnore
    Double getLongitude();

    @JsonIgnore
    String getLocationName();


    default List<String> getTags() {
        if (getTagsString().contains(",")) {
            return List.of(Strings.split(getTagsString(), ","));
        }
        return List.of(getTagsString());
    }

    default Location getLocation() {
        return new Location(
                getLatitude(),
                getLongitude(),
                getLocationName()
        );
    }
}
