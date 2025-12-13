package com.hive.app.events.model.response;

import com.hive.app.events.model.EventType;
import com.hive.app.events.model.Location;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class EventResponse {
    private Long id;
    private String title;
    private String description;
    private List<String> tags;
    private Location location;
    private Integer maxParticipants;
    private EventType type;
    private Integer duration;
    private LocalDate eventDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long ownerId;
    private boolean cancelled;
}
