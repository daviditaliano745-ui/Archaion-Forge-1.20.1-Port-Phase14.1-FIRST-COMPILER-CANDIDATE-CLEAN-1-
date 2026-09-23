package com.ratrod.archaion.registry;

import com.ratrod.archaion.Archaion;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ACSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Archaion.MODID);

    public static final RegistryObject<SoundEvent> BRAVE_AMBIENT = register("brave_ambient");
    public static final RegistryObject<SoundEvent> BRAVE_HURT = register("brave_hurt");
    public static final RegistryObject<SoundEvent> BRAVE_DEATH = register("brave_death");
    public static final RegistryObject<SoundEvent> BRAVE_JUMP = register("brave_jump");
    public static final RegistryObject<SoundEvent> ECHO_MACE_THROW = register("echo_mace_throw");
    public static final RegistryObject<SoundEvent> ECHO_STAR_BLAST = register("echo_star_blast");
    public static final RegistryObject<SoundEvent> LOD_ACTION_START = register("lod_action_start");
    public static final RegistryObject<SoundEvent> LOD_ACTIVATE = register("lod_activate");
    public static final RegistryObject<SoundEvent> LOD_ACTIVATE_SMASH = register("lod_activate_smash");
    public static final RegistryObject<SoundEvent> LOD_AMBIENT = register("lod_ambient");
    public static final RegistryObject<SoundEvent> LOD_DEATH = register("lod_death");
    public static final RegistryObject<SoundEvent> LOD_HURT = register("lod_hurt");
    public static final RegistryObject<SoundEvent> LOD_SHOOT = register("lod_shoot");
    public static final RegistryObject<SoundEvent> LOD_SMASH = register("lod_smash");
    public static final RegistryObject<SoundEvent> LOD_SPAWN_ARCHAICS = register("lod_spawn_archaics");
    public static final RegistryObject<SoundEvent> LOD_SPIN = register("lod_spin");
    public static final RegistryObject<SoundEvent> LOD_STEP = register("lod_step");
    public static final RegistryObject<SoundEvent> LOD_WARN_ARCHAICS = register("lod_warn_archaics");
    public static final RegistryObject<SoundEvent> LOD_ECHO_CHARGE_INTERACT = register("lod_echo_charge_interact");
    public static final RegistryObject<SoundEvent> LOD_BLOCK_FALL = register("lod_block_fall");
    public static final RegistryObject<SoundEvent> LOD_THEME_PHASE_1 = register("lod_boss_theme_phase_1");
    public static final RegistryObject<SoundEvent> LOD_THEME_PHASE_2 = register("lod_boss_theme_phase_2");
    public static final RegistryObject<SoundEvent> LOD_THEME_PHASE_3 = register("lod_boss_theme_phase_3");
    public static final RegistryObject<SoundEvent> SENTINEL_AMBIENT = register("sentinel_ambient");
    public static final RegistryObject<SoundEvent> SENTINEL_HURT = register("sentinel_hurt");
    public static final RegistryObject<SoundEvent> SENTINEL_DEATH = register("sentinel_death");
    public static final RegistryObject<SoundEvent> SENTINEL_START_CHARGING = register("sentinel_start_charging");
    public static final RegistryObject<SoundEvent> HAUNTER_AMBIENT = register("haunter_ambient");
    public static final RegistryObject<SoundEvent> HAUNTER_EXPLODE = register("haunter_explode");
    public static final RegistryObject<SoundEvent> HAUNTER_HURT = register("haunter_hurt");
    public static final RegistryObject<SoundEvent> TELEPORTER_WARPS = register("teleporter_warps");

    private static RegistryObject<SoundEvent> register(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(Archaion.prefix(name)));
    }
    private ACSounds() {}
}
