package com.hive.app.events.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class EventRequest {
    private String title;
    private String description;
    private List<String> tags;
    private Location location;
    private Integer maxParticipants;
    private EventType type;
    private Integer duration;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
