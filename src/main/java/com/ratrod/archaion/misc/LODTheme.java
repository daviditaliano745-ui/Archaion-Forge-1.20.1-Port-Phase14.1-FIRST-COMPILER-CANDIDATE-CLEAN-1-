package com.ratrod.archaion.misc;

import com.ratrod.archaion.registry.ACSounds;
import net.minecraft.sounds.SoundEvent;

/** Music phases used by The Last of Deepslate. */
public enum LODTheme {
    PHASE_1,
    PHASE_2,
    PHASE_3,
    STOP;

    public SoundEvent sound() {
        return switch (this) {
            case PHASE_1 -> ACSounds.LOD_THEME_PHASE_1.get();
            case PHASE_2 -> ACSounds.LOD_THEME_PHASE_2.get();
            case PHASE_3 -> ACSounds.LOD_THEME_PHASE_3.get();
            case STOP -> throw new IllegalStateException("LODTheme.STOP has no sound event");
        };
    }

    /** Original mod treats 1/2/3 as the three music phases. */
    public static LODTheme fromPhase(int phase) {
        return switch (phase) {
            case 2 -> PHASE_2;
            case 3 -> PHASE_3;
            default -> PHASE_1;
        };
    }

    public static LODTheme fromBossPhase(int zeroBasedPhase) {
        return fromPhase(Math.max(1, zeroBasedPhase + 1));
    }
}
