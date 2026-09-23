package com.ratrod.archaion.client.misc;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Client cache keyed by the vanilla boss-event UUID. */
public final class ClientBossBarData {
    private static final Map<UUID, DynamicData> BOSS_INDICES = new HashMap<>();

    public static void setBossData(UUID bossBarId, int bossIdx, Map<String, Integer> values) {
        BOSS_INDICES.put(bossBarId, new DynamicData(bossIdx, Map.copyOf(values)));
    }

    public static int getBossIdx(UUID bossBarId) {
        DynamicData data = BOSS_INDICES.get(bossBarId);
        return data != null ? data.id() : -1;
    }

    public static Map<String, Integer> getValues(UUID bossBarId) {
        DynamicData data = BOSS_INDICES.get(bossBarId);
        return data != null ? data.values() : Map.of();
    }

    public static void removeBossBar(UUID bossBarId) {
        BOSS_INDICES.remove(bossBarId);
    }

    private record DynamicData(int id, Map<String, Integer> values) { }

    private ClientBossBarData() { }
}
