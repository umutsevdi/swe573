package com.hive.app.events.model;


import java.util.List;
import java.util.Map;

/**
 * == ParticipationState Finite State Machine ==
 * \     ┌──────┐          ┌─────────────┐         ┌──────────┐
 * \  ──>│ SENT │─────────>│ NEGOTIATING │──+─────>│ ACCEPTED │
 * \     └──────┘          └─────────────┘  │      └──────────┘
 * \         │                              │
 * \         │                              │
 * \         └───────────────────────┐      │
 * \                                 │      │
 * \              ┌───────────┐      │      │       ┌──────────┐
 * \              │ WITHDRAWN │<─────+──────+──────>│ REJECTED │
 * \              └───────────┘                     └──────────┘
 */
public enum ParticipationState {
    SENT,
    NEGOTIATING,
    ACCEPTED,
    REJECTED,
    WITHDRAWN,
    COMPLETED;

    private static final Map<ParticipationState, List<ParticipationState>> OPTIONS =
            Map.of(
                    SENT, List.of(NEGOTIATING, REJECTED, WITHDRAWN),
                    NEGOTIATING, List.of(ACCEPTED, REJECTED, WITHDRAWN),
                    WITHDRAWN, List.of(SENT)
            );

    public static List<ParticipationState> getOptions(ParticipationState option) {
        return OPTIONS.getOrDefault(option, List.of());
    }
}
