package com.hive.app.events.model.request;

import com.hive.app.events.model.ParticipationState;
import lombok.Data;

@Data
public class ParticipationAssignmentRequest {
    Long userId;
    ParticipationState state;
}
