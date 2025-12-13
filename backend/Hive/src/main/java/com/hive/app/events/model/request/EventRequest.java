package com.hive.app.events.model.request;

import com.hive.app.events.model.EventType;
import com.hive.app.events.model.Location;
import lombok.Data;

import java.time.LocalDate;
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
    private LocalDate eventDate;
}
