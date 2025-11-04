package com.hive.app.events.model;

import com.hive.app.account.model.UserDTO;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class EventDTO {
    private Long id;
    String title;
    String description;
    List<String> tags;
    String backgroundImage;
    Location location;
    Integer maxParticipants;
    EventType type;
    Integer duration;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    UserDTO owner;
}
