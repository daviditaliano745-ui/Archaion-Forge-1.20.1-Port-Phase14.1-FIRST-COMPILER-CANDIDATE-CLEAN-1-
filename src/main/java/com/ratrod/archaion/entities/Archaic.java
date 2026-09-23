package com.ratrod.archaion.entities;

import java.util.UUID;

/** Shared state used by the Ancient/Archaic mobs. */
public interface Archaic {
    int CHARGED_XP_MULTIPLIER = 20;

    boolean isCharged();
    void setCharged(boolean charged);
    UUID getOwnerUUID();
    void setOwnerUUID(UUID ownerUUID);

    default int archaicXpReward(int baseXp) {
        return baseXp * (isCharged() ? CHARGED_XP_MULTIPLIER : 1);
    }
}
