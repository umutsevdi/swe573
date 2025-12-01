package com.hive.app.events.model;


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

    public static boolean isResolved(ParticipationState state) {
        return !(state == ParticipationState.SENT ||
                state == ParticipationState.NEGOTIATING);
    }

    public static boolean isRequestPermittedForOwner(ParticipationState state) {
        return state == ParticipationState.ACCEPTED || state == ParticipationState.REJECTED;
    }
}
