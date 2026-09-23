package com.ratrod.archaion.entities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PowerableMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import javax.annotation.Nullable;

import java.util.UUID;

/** Forge 1.20.1 port of Archaion's Slated. */
public class Slated extends Zombie implements Archaic, PowerableMob {
    private static final EntityDataAccessor<Boolean> IS_CHARGED =
            SynchedEntityData.defineId(Slated.class, EntityDataSerializers.BOOLEAN);

    @Nullable
    private UUID ownerUUID;

    public Slated(EntityType<? extends Slated> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(IS_CHARGED, false);
    }

    @Override public boolean isCharged() { return entityData.get(IS_CHARGED); }
    @Override public boolean isPowered() { return isCharged(); }
    @Override public void setCharged(boolean charged) { entityData.set(IS_CHARGED, charged); }
    @Override public UUID getOwnerUUID() { return ownerUUID; }
    @Override public void setOwnerUUID(UUID ownerUUID) { this.ownerUUID = ownerUUID; }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setCharged(tag.getBoolean("isCharged"));
        if (tag.contains("ownerUUID")) {
            try { ownerUUID = UUID.fromString(tag.getString("ownerUUID")); }
            catch (IllegalArgumentException ignored) { ownerUUID = null; }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("isCharged", isCharged());
        if (ownerUUID != null) tag.putString("ownerUUID", ownerUUID.toString());
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Zombie.createAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.23D)
                .add(Attributes.ATTACK_DAMAGE, 10.0D)
                .add(Attributes.FOLLOW_RANGE, 50.0D);
    }

    @Override public boolean isBaby() { return false; }
    @Override public void setBaby(boolean baby) { }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
                                        @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {
        return super.finalizeSpawn(level, difficulty, reason, new ZombieGroupData(false, true), dataTag);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && getMainHandItem().isEmpty() && target instanceof net.minecraft.world.entity.LivingEntity living) {
            float difficulty = level().getCurrentDifficultyAt(blockPosition()).getEffectiveDifficulty();
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 140 * (int)difficulty), this);
        }
        return hit;
    }

    @Override
    protected boolean convertsInWater() {
        return false;
    }
}
