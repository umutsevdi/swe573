package com.hive.app.events.model;

import lombok.Data;

@Data
public class ParticipationAssignmentRequest {
    Long userId;
    ParticipationState state;
}
