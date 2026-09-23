package com.ratrod.archaion.api.trial;

import net.minecraft.util.StringRepresentable;

/** 1.20.1 mirror of the vanilla 1.21 trial-spawner visual/gameplay states. */
public enum TrialSpawnerState implements StringRepresentable {
    INACTIVE("inactive"),
    WAITING_FOR_PLAYERS("waiting_for_players"),
    ACTIVE("active"),
    WAITING_FOR_REWARD_EJECTION("waiting_for_reward_ejection"),
    EJECTING_REWARD("ejecting_reward"),
    COOLDOWN("cooldown");

    private final String name;
    TrialSpawnerState(String name) { this.name = name; }
    @Override public String getSerializedName() { return name; }
}
