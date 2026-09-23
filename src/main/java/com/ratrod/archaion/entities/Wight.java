package com.ratrod.archaion.entities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PowerableMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import javax.annotation.Nullable;

import java.util.UUID;

/** Forge 1.20.1 port of Archaion's Wight. */
public class Wight extends Skeleton implements Archaic, PowerableMob {
    private static final EntityDataAccessor<Boolean> IS_CHARGED =
            SynchedEntityData.defineId(Wight.class, EntityDataSerializers.BOOLEAN);

    @Nullable
    private UUID ownerUUID;

    public Wight(EntityType<? extends Wight> type, Level level) {
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
        return Monster.createMonsterAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.MAX_HEALTH, 22.0D);
    }

    @Override
    protected AbstractArrow getArrow(ItemStack arrowStack, float velocity) {
        AbstractArrow arrow = super.getArrow(arrowStack, velocity);
        if (arrow instanceof Arrow vanillaArrow) {
            vanillaArrow.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 300));
            arrow.setBaseDamage(6.0D);
        }
        return arrow;
    }

    @Override protected SoundEvent getAmbientSound() { return SoundEvents.STRAY_AMBIENT; }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.STRAY_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.STRAY_DEATH; }
    @Override protected SoundEvent getStepSound() { return SoundEvents.STRAY_STEP; }
}
