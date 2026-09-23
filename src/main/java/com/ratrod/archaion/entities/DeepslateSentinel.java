package com.ratrod.archaion.entities;

import com.ratrod.archaion.entities.ai.goals.PickUpRidersGoal;
import com.ratrod.archaion.entities.ai.goals.SentinelChargeGoal;
import com.ratrod.archaion.registry.ACSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PowerableMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Forge 1.20.1 gameplay port of Archaion's Deepslate Sentinel.
 *
 * The 1.21 ActionManager charge has been translated to a native Goal while
 * preserving its wind-up, acceleration window, repeated shockwaves, stuck
 * detection and cooldown. Custom model/animation playback is deferred to the
 * dedicated client phase.
 */
public class DeepslateSentinel extends Monster implements Archaic, PowerableMob {
    private static final EntityDataAccessor<Boolean> IS_CHARGED =
            SynchedEntityData.defineId(DeepslateSentinel.class, EntityDataSerializers.BOOLEAN);

    private int chargeCooldownTicks;
    @Nullable
    private UUID ownerUUID;

    public DeepslateSentinel(EntityType<? extends DeepslateSentinel> type, Level level) {
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
                .add(Attributes.MAX_HEALTH, 100.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.30D)
                .add(Attributes.ATTACK_DAMAGE, 15.0D)
                .add(Attributes.ARMOR, 8.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 4.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.FOLLOW_RANGE, 48.0D);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        // Rider pickup must win over charging, matching the original canStart gate.
        goalSelector.addGoal(1, new PickUpRidersGoal(this, 1.0D, 32.0D));
        goalSelector.addGoal(2, new SentinelChargeGoal(this));
        goalSelector.addGoal(3, new MoveTowardsTargetGoal(this, 1.0D, 48.0F));

        targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void tick() {
        if (chargeCooldownTicks > 0) chargeCooldownTicks--;
        super.tick();
    }

    public boolean isChargeOnCooldown() {
        return chargeCooldownTicks > 0;
    }

    public void startChargeCooldown() {
        chargeCooldownTicks = random.nextInt(41) + 180; // 180..220 inclusive
    }

    public boolean hasNearbyRidersToPickup(double radius) {
        if (!getPassengers().isEmpty()) return false;
        AABB box = getBoundingBox().inflate(radius);
        return !level().getEntitiesOfClass(Wight.class, box,
                wight -> wight.isAlive() && !wight.isPassenger()).isEmpty();
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return getPassengers().isEmpty() && passenger instanceof Wight;
    }

    @Override
    public double getPassengersRidingOffset() {
        // 1.21 used an attachment point near the Sentinel's upper back.
        return getBbHeight() * 0.80D;
    }

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        // While a Wight is available, the Sentinel pauses normal controlled movement
        // long enough for the pickup goal to collect it.
        return hasNearbyRidersToPickup(32.0D) ? null : super.getControllingPassenger();
    }

    @Override protected SoundEvent getAmbientSound() { return ACSounds.SENTINEL_AMBIENT.get(); }
    @Override protected float getSoundVolume() { return 2.0F; }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return ACSounds.SENTINEL_HURT.get(); }
    @Override protected SoundEvent getDeathSound() { return ACSounds.SENTINEL_DEATH.get(); }
}
