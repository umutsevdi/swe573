package com.hive.app.events.model;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class EventQueryOptions {
    Location userLocation;
    Integer proximity;
    LocalDate startDate;
    LocalDate endDate;
    String textSearch;
    List<String> tags;
    EventType type;
    boolean includeCancelled = false;
}
