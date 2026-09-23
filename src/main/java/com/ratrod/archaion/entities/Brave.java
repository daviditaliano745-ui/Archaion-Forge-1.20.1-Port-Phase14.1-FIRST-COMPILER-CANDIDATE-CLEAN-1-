package com.ratrod.archaion.entities;

import com.ratrod.archaion.entities.ai.goals.BraveDistanceAwayGoal;
import com.ratrod.archaion.entities.ai.goals.BraveLeapGoal;
import com.ratrod.archaion.entities.ai.goals.BraveSpreadTargetGoal;
import com.ratrod.archaion.registry.ACSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PowerableMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.UUID;

/** Forge 1.20.1 gameplay port of Archaion's Brave. */
public class Brave extends Monster implements Archaic, PowerableMob {
    private static final EntityDataAccessor<Boolean> IS_CHARGED =
            SynchedEntityData.defineId(Brave.class, EntityDataSerializers.BOOLEAN);

    @Nullable
    private UUID ownerUUID;

    public Brave(EntityType<? extends Brave> type, Level level) {
        super(type, level);
        setPathfindingMalus(BlockPathTypes.TRAPDOOR, -1.0F);
        setPathfindingMalus(BlockPathTypes.DANGER_FIRE, -1.0F);
        this.xpReward = 20;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.35D)
                .add(Attributes.MAX_HEALTH, 55.0D)
                .add(Attributes.FOLLOW_RANGE, 48.0D)
                .add(Attributes.ATTACK_DAMAGE, 12.0D);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        // The 1.21 action-manager attack is represented as a native 1.20.1 goal.
        goalSelector.addGoal(1, new BraveLeapGoal(this));
        goalSelector.addGoal(2, new BraveDistanceAwayGoal(this, 1.1D, 18.0D));

        targetSelector.addGoal(1, new BraveSpreadTargetGoal(this));
        targetSelector.addGoal(2, new HurtByTargetGoal(this));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
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

    @Nullable
    public LivingEntity getOwner() {
        if (ownerUUID == null || !(level() instanceof ServerLevel serverLevel)) return null;
        Entity entity = serverLevel.getEntity(ownerUUID);
        return entity instanceof LivingEntity living ? living : null;
    }

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

    /** Original Brave considers a target inside a 15 x 15 cylinder to be too close. */
    public boolean mustRetreat(Vec3 targetPos) {
        Vec3 here = Vec3.atCenterOf(blockPosition());
        double dx = targetPos.x - here.x;
        double dz = targetPos.z - here.z;
        return dx * dx + dz * dz < 225.0D && Math.abs(targetPos.y - here.y) < 15.0D;
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        EntityType<?> type = target.getType();
        return (type == EntityType.PLAYER || type == EntityType.IRON_GOLEM) && super.canAttack(target);
    }

    @Override
    public int getMaxHeadYRot() {
        return 30;
    }

    @Override protected SoundEvent getAmbientSound() { return ACSounds.BRAVE_AMBIENT.get(); }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return ACSounds.BRAVE_HURT.get(); }
    @Override protected SoundEvent getDeathSound() { return ACSounds.BRAVE_DEATH.get(); }

    @Override
    protected void checkFallDamage(double yMotion, boolean onGround, BlockState state, BlockPos pos) {
        // Original Brave suppresses normal fall/landing damage; its leap owns the landing event.
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        // Intentionally silent, matching the original class.
    }
}
