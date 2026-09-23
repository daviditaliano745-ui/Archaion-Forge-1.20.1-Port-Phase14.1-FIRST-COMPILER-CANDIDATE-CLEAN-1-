package com.ratrod.archaion.entities;

import com.ratrod.archaion.Archaion;
import com.ratrod.archaion.entities.ai.controls.look.LastOfDeepslateLookControl;
import com.ratrod.archaion.entities.ai.controls.move.ACMoveControl;
import com.ratrod.archaion.entities.ai.goals.LODAttackableRandomTargetGoal;
import com.ratrod.archaion.entities.ai.goals.LODBodySlamGoal;
import com.ratrod.archaion.entities.ai.goals.LODRollGoal;
import com.ratrod.archaion.entities.ai.goals.LODSpawnArchaicsGoal;
import com.ratrod.archaion.entities.ai.goals.LODSmashGroundGoal;
import com.ratrod.archaion.entities.ai.goals.LODInterceptShootGoal;
import com.ratrod.archaion.entities.ai.goals.LODShootGoal;
import com.ratrod.archaion.entities.ai.goals.LODSwingSpinGoal;
import com.ratrod.archaion.entities.ai.systems.ArchaicRaid;
import com.ratrod.archaion.registry.ACEffects;
import com.ratrod.archaion.registry.ACItems;
import com.ratrod.archaion.registry.ACSounds;
import com.ratrod.archaion.misc.LODTheme;
import com.ratrod.archaion.network.ACNetwork;
import com.ratrod.archaion.network.BossBarDataOutput;
import com.ratrod.archaion.network.s2c.BossMusicPacket;
import com.ratrod.archaion.network.s2c.CameraShakePacket;
import com.ratrod.archaion.network.s2c.SyncBossBarDataPacket;
import com.ratrod.archaion.network.s2c.RemoveBossBarDataPacket;
import mod.chloeprime.aaaparticles.api.common.AAALevel;
import mod.chloeprime.aaaparticles.api.common.ParticleEmitterInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.BossEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PowerableMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsTargetGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * Forge 1.20.1 core gameplay port of The Last of Deepslate.
 *
 * Forge 1.20.1 gameplay port of the boss core: awakening, raid protection/anti-burst,
 * physical/ranged attacks, Roll, player-scaled Archaic phase transitions, boss music,
 * custom HUD sync, and authored model presentation. Exact legacy ActionManager attack
 * keyframe playback remains intentionally replaced by the native AI/attack architecture.
 */
public class LastOfDeepslate extends Monster implements PowerableMob {
    private static final EntityDataAccessor<Integer> SLEEPING_STATE =
            SynchedEntityData.defineId(LastOfDeepslate.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> HAS_CHARGED_ARCHAICS =
            SynchedEntityData.defineId(LastOfDeepslate.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> ECHO_CHARGES_FED =
            SynchedEntityData.defineId(LastOfDeepslate.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> RAID_PHASE =
            SynchedEntityData.defineId(LastOfDeepslate.class, EntityDataSerializers.INT);

    private final ServerBossEvent bossEvent = new ServerBossEvent(
            Component.translatable("entity.archaion.last_of_deepslate"),
            BossEvent.BossBarColor.BLUE, BossEvent.BossBarOverlay.PROGRESS);
    private final ArchaicRaid archaicSystem = new ArchaicRaid(this);

    public int shootingCooldown;
    public int wakingStartTick;
    private boolean actionLocked;
    private boolean raidSpawning;
    private boolean headTrackingSuppressed;
    private LODTheme musicPhase = LODTheme.PHASE_1;

    public LastOfDeepslate(EntityType<? extends LastOfDeepslate> type, Level level) {
        super(type, level);
        // The original boss only has 15% of normal yaw freedom while pathing, which
        // is part of its deliberately heavy/slow turning feel.
        this.moveControl = new ACMoveControl(this, 0.15F);
        this.lookControl = new LastOfDeepslateLookControl(this);
        // 1.21 exposed this as Attributes.STEP_HEIGHT=1.5. In 1.20.1 the
        // equivalent entity-level setter preserves the same effective step height.
        this.setMaxUpStep(1.5F);
        bossEvent.setVisible(false);
        setNoAi(true);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(SLEEPING_STATE, SleepingState.SLEEPING.ordinal());
        entityData.define(HAS_CHARGED_ARCHAICS, false);
        entityData.define(ECHO_CHARGES_FED, 0);
        entityData.define(RAID_PHASE, 0);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 600.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, 35.0D)
                .add(Attributes.FOLLOW_RANGE, 64.0D)
                .add(Attributes.ARMOR, 15.0D)
                // The original uses 2.0; 1.20.1's vanilla attribute range caps effective resistance at 1.
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new LODSpawnArchaicsGoal(this));
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(2, new LODRollGoal(this));
        goalSelector.addGoal(3, new LODBodySlamGoal(this));
        goalSelector.addGoal(4, new LODSwingSpinGoal(this));
        goalSelector.addGoal(5, new LODSmashGroundGoal(this));
        goalSelector.addGoal(6, new LODInterceptShootGoal(this));
        goalSelector.addGoal(7, new LODShootGoal(this));
        goalSelector.addGoal(9, new MoveTowardsTargetGoal(this, 1.25D, 64.0F));

        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new LODAttackableRandomTargetGoal(this));
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide) {
            bossEvent.setProgress(Math.max(0.0F, getHealth() / getMaxHealth()));
            archaicSystem.tick();
            if (shootingCooldown > 0) shootingCooldown--;

            if (getSleepingState() == SleepingState.WAKING) {
                int elapsed = tickCount - wakingStartTick;
                if (elapsed == 45) {
                    playSound(ACSounds.LOD_ACTIVATE_SMASH.get(), 3.0F, 1.0F);
                    ACNetwork.sendToTrackingPlayers(this, new CameraShakePacket(1.25F, 24, 8.0F));
                    bossEvent.setVisible(true);
                }
                if (elapsed >= 80) {
                    setSleepingState(SleepingState.AWAKE);
                    setNoAi(false);
                    setMusicPhase(LODTheme.PHASE_1);
                }
            }
        } else if (getSleepingState() != SleepingState.SLEEPING) {
            if (tickCount % 8 == 0) {
                AAALevel.addParticle(level(), new ParticleEmitterInfo(Archaion.prefix("lod_aura"))
                        .position(position().add(getDeltaMovement().scale(3.0D)).add(0.0D, 0.5D, 0.0D))
                        .scale(1.25F));
            }
            if (getPhase() >= 2 && tickCount % 20 == 0) {
                AAALevel.addParticle(level(), new ParticleEmitterInfo(Archaion.prefix("lod_smoking"))
                        .position(position().add(0.0D, 3.0D, 0.0D)).scale(1.5F));
            }
        }
    }

    public SleepingState getSleepingState() { return SleepingState.byId(entityData.get(SLEEPING_STATE)); }
    public void setSleepingState(SleepingState state) { entityData.set(SLEEPING_STATE, state.ordinal()); }
    public boolean hasChargedArchaics() { return entityData.get(HAS_CHARGED_ARCHAICS); }
    public void setHasChargedArchaics(boolean value) { entityData.set(HAS_CHARGED_ARCHAICS, value); }
    public int getEchoChargesFed() { return entityData.get(ECHO_CHARGES_FED); }
    public void setEchoChargesFed(int value) { entityData.set(ECHO_CHARGES_FED, value); }
    public int getPhase() { return entityData.get(RAID_PHASE); }
    public void setPhase(int phase) {
        int safePhase = Math.max(0, phase);
        int oldPhase = getPhase();
        entityData.set(RAID_PHASE, safePhase);
        if (!level().isClientSide && safePhase != oldPhase && getSleepingState() == SleepingState.AWAKE) {
            setMusicPhase(LODTheme.fromBossPhase(safePhase));
        }
    }
    public ArchaicRaid getArchaicSystem() { return archaicSystem; }
    public ServerBossEvent getBossEvent() { return bossEvent; }

    private SyncBossBarDataPacket createBossBarDataPacket(int chargedAlive) {
        BossBarDataOutput output = new BossBarDataOutput();
        archaicSystem.writeBossBarData(output, chargedAlive);
        return new SyncBossBarDataPacket(bossEvent.getId(), 0, output.build());
    }

    private SyncBossBarDataPacket createBossBarDataPacket() {
        BossBarDataOutput output = new BossBarDataOutput();
        archaicSystem.writeBossBarData(output);
        return new SyncBossBarDataPacket(bossEvent.getId(), 0, output.build());
    }

    public void syncBossBarData(int chargedAlive) {
        if (level().isClientSide) return;
        SyncBossBarDataPacket packet = createBossBarDataPacket(chargedAlive);
        for (ServerPlayer player : bossEvent.getPlayers()) {
            ACNetwork.sendToPlayer(player, packet);
        }
    }

    public LODTheme getMusicPhase() { return musicPhase; }

    public void setMusicPhase(LODTheme theme) {
        if (theme == null || theme == LODTheme.STOP) return;
        if (musicPhase == theme && getSleepingState() == SleepingState.AWAKE) {
            sendBossMusic(theme);
            return;
        }
        musicPhase = theme;
        sendBossMusic(theme);
    }

    public void sendBossMusic(LODTheme theme) {
        if (level().isClientSide) return;
        for (ServerPlayer player : bossEvent.getPlayers()) {
            ACNetwork.sendToPlayer(player, new BossMusicPacket(theme));
        }
    }

    @Override public boolean isPowered() { return hasChargedArchaics(); }
    @Override public boolean isPersistenceRequired() { return true; }
    @Override public boolean isPushable() { return false; }
    @Override protected void pushEntities() { /* Original boss deliberately does not body-push nearby entities. */ }

    public boolean isActionLocked() { return actionLocked; }
    public void setActionLocked(boolean locked) { actionLocked = locked; }
    public boolean isRaidSpawning() { return raidSpawning; }
    public void setRaidSpawning(boolean value) { raidSpawning = value; }
    public boolean isHeadTrackingSuppressed() { return headTrackingSuppressed; }
    public void setHeadTrackingSuppressed(boolean value) { headTrackingSuppressed = value; }
    public boolean isCombatReady() { return getSleepingState() == SleepingState.AWAKE && !isNoAi() && !raidSpawning; }

    @Override
    public boolean canAttack(LivingEntity target) {
        return !archaicSystem.isOwnedArchaic(target) && super.canAttack(target);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (getSleepingState() != SleepingState.AWAKE || raidSpawning) return false;

        int charged = archaicSystem.countChargedArchaics();
        amount *= archaicSystem.getArchaicProtectionMultiplier(charged);

        float threshold = getMaxHealth() * 0.04F;
        if (amount > threshold) amount *= getAntiBurstMultiplier(amount, threshold);

        if (charged > 0 && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            amount = Math.min(amount, Math.max(0.0F, getHealth() - 1.0F));
        }
        return amount > 0.0F && super.hurt(source, amount);
    }

    public float getAntiBurstMultiplier(float damage, float threshold) {
        int players = Math.max(1, archaicSystem.countNearbyPlayers());
        float base = Math.max(0.05F, 0.20F - 0.03F * (players - 1));
        float excess = damage - threshold;
        return base + (1.0F - base) / (1.0F + excess / threshold);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof LivingEntity living) {
            int amplifier = getPhase() >= 2 ? 1 : (getPhase() == 1 ? 0 : -1);
            if (amplifier >= 0) living.addEffect(new MobEffectInstance(ACEffects.ARMOR_BREAK.get(), 200, amplifier), this);
        }
        return hit;
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (getSleepingState() == SleepingState.SLEEPING && stack.is(ACItems.ECHO_CHARGE.get())) {
            if (!level().isClientSide) {
                if (!player.getAbilities().instabuild) stack.shrink(1);
                feedEchoCharge();
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    public void feedEchoCharge() {
        setEchoChargesFed(getEchoChargesFed() + 1);
        playSound(ACSounds.LOD_AMBIENT.get(), 3.0F, 1.0F);
        if (getEchoChargesFed() >= 4 && getSleepingState() == SleepingState.SLEEPING) beginWaking();
        playSound(ACSounds.LOD_ECHO_CHARGE_INTERACT.get(), 3.0F, 1.0F);
        AAALevel.addParticle(level(), new ParticleEmitterInfo(Archaion.prefix("lod_feed_charge"))
                .position(position().add(getDeltaMovement().scale(3.0D)).add(0.0D, 0.5D, 0.0D)).scale(1.75F));
    }

    private void beginWaking() {
        setSleepingState(SleepingState.WAKING);
        playSound(ACSounds.LOD_ACTIVATE.get(), 3.0F, 1.0F);
        wakingStartTick = tickCount;
        setNoAi(true);
    }

    public float attackDamage(float multiplier) {
        return (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * multiplier;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("sleepState", getSleepingState().ordinal());
        tag.putInt("echoChargesFed", getEchoChargesFed());
        tag.putInt("musicPhase", musicPhase.ordinal() + 1);
        archaicSystem.save(tag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("sleepState")) setSleepingState(SleepingState.byId(tag.getInt("sleepState")));
        setEchoChargesFed(tag.getInt("echoChargesFed"));
        if (tag.contains("musicPhase")) musicPhase = LODTheme.fromPhase(tag.getInt("musicPhase"));
        archaicSystem.load(tag);
        if (!tag.contains("musicPhase")) musicPhase = LODTheme.fromBossPhase(getPhase());
        boolean awake = getSleepingState() == SleepingState.AWAKE;
        setNoAi(!awake);
        bossEvent.setVisible(awake);
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        bossEvent.addPlayer(player);
        ACNetwork.sendToPlayer(player, createBossBarDataPacket());
        if (getSleepingState() == SleepingState.AWAKE) {
            ACNetwork.sendToPlayer(player, new BossMusicPacket(musicPhase));
        }
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bossEvent.removePlayer(player);
        ACNetwork.sendToPlayer(player, new RemoveBossBarDataPacket(bossEvent.getId()));
        ACNetwork.sendToPlayer(player, new BossMusicPacket(LODTheme.STOP));
    }

    @Override
    public void die(DamageSource source) {
        for (ServerPlayer player : bossEvent.getPlayers()) {
            ACNetwork.sendToPlayer(player, new RemoveBossBarDataPacket(bossEvent.getId()));
        }
        sendBossMusic(LODTheme.STOP);
        super.die(source);
    }

    @Override
    public void setCustomName(@Nullable Component name) {
        super.setCustomName(name);
        bossEvent.setName(getDisplayName());
    }

    @Nullable @Override protected SoundEvent getAmbientSound() {
        return getSleepingState() == SleepingState.AWAKE ? ACSounds.LOD_AMBIENT.get() : null;
    }
    @Override protected float getSoundVolume() { return 3.0F; }
    @Override protected SoundEvent getDeathSound() { return ACSounds.LOD_DEATH.get(); }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return ACSounds.LOD_HURT.get(); }
    @Override protected void playStepSound(BlockPos pos, BlockState state) {
        super.playStepSound(pos, state);
        playSound(ACSounds.LOD_STEP.get(), 1.0F, 1.0F);
    }
}
