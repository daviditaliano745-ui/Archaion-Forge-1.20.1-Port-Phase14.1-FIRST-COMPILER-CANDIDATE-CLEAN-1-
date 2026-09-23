package com.ratrod.archaion.network;

import java.util.HashMap;
import java.util.Map;

/** Compact builder for the integer-only values used by Archaion's custom boss HUD. */
public final class BossBarDataOutput {
    private final Map<String, Integer> values = new HashMap<>();

    public BossBarDataOutput add(String key, int value) {
        values.put(key, value);
        return this;
    }

    public Map<String, Integer> build() {
        return Map.copyOf(values);
    }
}
