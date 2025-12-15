package com.hive.app.events.model;

import com.hive.app.account.model.UserDTO;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class EventDTO {
    private Long id;
    private String title;
    private String description;
    private List<String> tags;
    private Location location;
    private Integer maxParticipants;
    private EventType type;
    private Integer duration;
    private LocalDateTime eventDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean cancelled;
    private UserDTO owner;
}
