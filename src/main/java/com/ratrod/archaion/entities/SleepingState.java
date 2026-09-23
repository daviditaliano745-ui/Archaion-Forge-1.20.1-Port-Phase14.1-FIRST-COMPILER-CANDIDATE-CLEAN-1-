package com.ratrod.archaion.entities;

/** 1.20.1 replacement for Archaion's custom sleeping-state data serializer. */
public enum SleepingState {
    SLEEPING,
    WAKING,
    AWAKE;

    public static SleepingState byId(int id) {
        SleepingState[] values = values();
        return id >= 0 && id < values.length ? values[id] : SLEEPING;
    }
}
