package com.ratrod.archaion.entities;

import com.ratrod.archaion.entities.ai.goals.GrimorayFlightGoal;
import com.ratrod.archaion.entities.ai.goals.GrimorayShootGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;

import javax.annotation.Nullable;

/** Forge 1.20.1 gameplay port of Archaion's Grimoray. */
public class Grimoray extends Monster {
    private static final EntityDataAccessor<Integer> GRIMORAY_TYPE =
            SynchedEntityData.defineId(Grimoray.class, EntityDataSerializers.INT);

    private int spellCooldown;

    public Grimoray(EntityType<? extends Grimoray> type, Level level) {
        super(type, level);
        setNoGravity(true);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new FlyingPathNavigation(this, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(GRIMORAY_TYPE, GrimorayType.POISON_CLOUD.ordinal());
    }

    public GrimorayType getGrimorayType() {
        int id = entityData.get(GRIMORAY_TYPE);
        GrimorayType[] values = GrimorayType.values();
        return id >= 0 && id < values.length ? values[id] : GrimorayType.POISON_CLOUD;
    }

    public void setGrimorayType(GrimorayType type) {
        entityData.set(GRIMORAY_TYPE, type.ordinal());
    }

    public int getSpellCooldown() { return spellCooldown; }
    public void setSpellCooldown(int spellCooldown) { this.spellCooldown = spellCooldown; }

    public int getSpellCDDuration() {
        return switch (getGrimorayType()) {
            case POISON_CLOUD -> 70;
            case HARMING -> 60;
            case HEALING -> 80;
        };
    }

    @Override
    public void tick() {
        super.tick();
        setNoGravity(true);
        if (!level().isClientSide && spellCooldown > 0) spellCooldown--;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("grimorayType")) {
            int type = tag.getInt("grimorayType");
            if (type >= 0 && type < GrimorayType.values().length) setGrimorayType(GrimorayType.values()[type]);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("grimorayType", getGrimorayType().ordinal());
    }

    @Override public boolean isNoGravity() { return true; }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 24.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.4D)
                .add(Attributes.FLYING_SPEED, 0.4D)
                .add(Attributes.FOLLOW_RANGE, 64.0D);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new GrimorayFlightGoal(this, 0.35D, 3.0D, 6.0D));
        // Shooting has no MOVE flag, so it can cast while orbiting as the original action system did.
        goalSelector.addGoal(1, new GrimorayShootGoal(this));
        goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8.0F));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType reason, @Nullable SpawnGroupData spawnData,
                                        @Nullable CompoundTag dataTag) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
        GrimorayType[] values = GrimorayType.values();
        setGrimorayType(values[random.nextInt(values.length)]);
        return result;
    }

    @Override protected SoundEvent getAmbientSound() { return SoundEvents.BOOK_PAGE_TURN; }

    @Override
    protected void checkFallDamage(double yMotion, boolean onGround, BlockState state, BlockPos pos) {
        // Flying mob; original suppresses fall handling.
    }
}
