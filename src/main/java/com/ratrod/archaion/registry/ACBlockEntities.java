package com.ratrod.archaion.registry;

import com.ratrod.archaion.Archaion;
import com.ratrod.archaion.block.HologramBlockEntity;
import com.ratrod.archaion.block.TeleporterBlockEntity;
import com.ratrod.archaion.api.trial.ACTrialSpawnerBlockEntity;
import com.ratrod.archaion.api.trial.TrialVaultBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ACBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Archaion.MODID);
    public static final RegistryObject<BlockEntityType<HologramBlockEntity>> HOLOGRAM = BLOCK_ENTITIES.register("hologram", () ->
            BlockEntityType.Builder.of(HologramBlockEntity::new, ACBlocks.DEEPSLATE_HOLOGRAM.get()).build(null));
    public static final RegistryObject<BlockEntityType<TeleporterBlockEntity>> TELEPORTER = BLOCK_ENTITIES.register("teleporter", () ->
            BlockEntityType.Builder.of(TeleporterBlockEntity::new, ACBlocks.DEEPSLATE_TELEPORTER.get()).build(null));
    public static final RegistryObject<BlockEntityType<ACTrialSpawnerBlockEntity>> TRIAL_SPAWNER = BLOCK_ENTITIES.register("trial_spawner", () ->
            BlockEntityType.Builder.of(ACTrialSpawnerBlockEntity::new, ACBlocks.DEEPSLATE_SPAWNER.get()).build(null));
    public static final RegistryObject<BlockEntityType<TrialVaultBlockEntity>> TRIAL_VAULT = BLOCK_ENTITIES.register("trial_vault", () ->
            BlockEntityType.Builder.of(TrialVaultBlockEntity::new, ACBlocks.DEEPSLATE_VAULT.get()).build(null));
    private ACBlockEntities() {}
}
