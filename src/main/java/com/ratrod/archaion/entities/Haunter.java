package com.ratrod.archaion.entities;

import com.ratrod.archaion.entities.ai.goals.HaunterExplodeGoal;
import com.ratrod.archaion.registry.ACSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PowerableMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.UUID;

/** Forge 1.20.1 port of Archaion's Haunter. */
public class Haunter extends Monster implements Archaic, PowerableMob {
    private static final EntityDataAccessor<Boolean> IS_CHARGED =
            SynchedEntityData.defineId(Haunter.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_SWELLING =
            SynchedEntityData.defineId(Haunter.class, EntityDataSerializers.BOOLEAN);

    public int explodingCooldown;
    @Nullable private UUID ownerUUID;

    public Haunter(EntityType<? extends Haunter> type, Level level) {
        super(type, level);
        this.xpReward = 12;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(IS_CHARGED, false);
        entityData.define(IS_SWELLING, false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.30D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new HaunterExplodeGoal(this));
        goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0D, false));
        targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && explodingCooldown > 0) explodingCooldown--;
    }

    public boolean isSwelling() { return entityData.get(IS_SWELLING); }
    public void setSwelling(boolean swelling) { entityData.set(IS_SWELLING, swelling); }

    @Override public boolean isCharged() { return entityData.get(IS_CHARGED); }
    @Override public boolean isPowered() { return isCharged(); }
    @Override public void setCharged(boolean charged) { entityData.set(IS_CHARGED, charged); }
    @Override public UUID getOwnerUUID() { return ownerUUID; }
    @Override public void setOwnerUUID(UUID ownerUUID) { this.ownerUUID = ownerUUID; }

    @Override
    public boolean canAttack(LivingEntity target) {
        return (target instanceof Player || target instanceof IronGolem) && super.canAttack(target);
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        // The original Haunter does not perform normal melee damage; reaching melee range
        // is only used to trigger its swelling/explosion action.
        return false;
    }

    @Override
    public int getExperienceReward() {
        return archaicXpReward(super.getExperienceReward());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("isCharged", isCharged());
        tag.putInt("explodingCooldown", explodingCooldown);
        if (ownerUUID != null) tag.putString("ownerUUID", ownerUUID.toString());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setCharged(tag.getBoolean("isCharged"));
        explodingCooldown = tag.getInt("explodingCooldown");
        if (tag.contains("ownerUUID")) {
            try { ownerUUID = UUID.fromString(tag.getString("ownerUUID")); }
            catch (IllegalArgumentException ignored) { ownerUUID = null; }
        }
    }

    @Override protected SoundEvent getAmbientSound() { return ACSounds.HAUNTER_AMBIENT.get(); }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return ACSounds.HAUNTER_HURT.get(); }
    @Override protected SoundEvent getDeathSound() { return ACSounds.HAUNTER_HURT.get(); }
}
