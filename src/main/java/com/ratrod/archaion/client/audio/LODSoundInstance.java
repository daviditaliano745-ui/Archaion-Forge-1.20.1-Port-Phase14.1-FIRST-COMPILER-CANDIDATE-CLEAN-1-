package com.ratrod.archaion.client.audio;

import com.ratrod.archaion.misc.LODTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;

/** Client-only looping/fading boss music, matching the original 0.025 volume fade step. */
public final class LODSoundInstance extends AbstractTickableSoundInstance {
    private static final float FADE_STEP = 0.025F;
    private static LODSoundInstance current;
    private static Level startedLevel;
    private static LODTheme currentTheme;
    private boolean fadingOut;

    private LODSoundInstance(SoundEvent sound) {
        super(sound, SoundSource.RECORDS, SoundInstance.createUnseededRandom());
        this.looping = true;
        this.delay = 0;
        this.volume = 1.0F;
        this.relative = true;
    }

    @Override
    public void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.level != startedLevel) {
            stop();
            current = null;
            currentTheme = null;
            startedLevel = minecraft.level;
            return;
        }
        if (fadingOut) {
            volume = Math.max(0.0F, volume - FADE_STEP);
            if (volume <= 0.0F) {
                stop();
                current = null;
                currentTheme = null;
            }
        }
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }

    public static void startPhase(LODTheme theme) {
        if (theme == null || theme == LODTheme.STOP) {
            fadeOut();
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return;
        if (current != null && currentTheme == theme && startedLevel == minecraft.level) {
            current.fadingOut = false;
            current.volume = 1.0F;
            return;
        }
        if (current != null) current.stop();
        currentTheme = theme;
        startedLevel = minecraft.level;
        current = new LODSoundInstance(theme.sound());
        minecraft.getSoundManager().play(current);
    }

    public static void fadeOut() {
        if (current != null) current.fadingOut = true;
    }

    /** Re-start the active phase if another sound-system action unexpectedly dropped it. */
    public static void tryToRepair() {
        if (current == null || current.fadingOut || currentTheme == null) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.level != startedLevel) return;
        if (!minecraft.getSoundManager().isActive(current)) startPhase(currentTheme);
    }

    public static boolean isActive() {
        if (current == null) return false;
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.level != null && minecraft.getSoundManager().isActive(current);
    }
}
