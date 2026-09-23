package com.ratrod.archaion.entities.ai.goals;

import com.ratrod.archaion.Archaion;
import com.ratrod.archaion.entities.Archaic;
import com.ratrod.archaion.entities.LastOfDeepslate;
import com.ratrod.archaion.registry.ACEntityTypes;
import com.ratrod.archaion.registry.ACSounds;
import mod.chloeprime.aaaparticles.api.common.AAALevel;
import mod.chloeprime.aaaparticles.api.common.ParticleEmitterInfo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/** Phase-transition raid port of LODSpawnArchaicsAction. */
public final class LODSpawnArchaicsGoal extends Goal {
    private final LastOfDeepslate boss;
    private final int[] batchSizes = new int[3];
    private int timer;
    private int batchIndex;
    private int smashIndex;
    private int regenTicksLeft;
    private float regenPerTick;
    private float statMultiplier = 1.1F;

    public LODSpawnArchaicsGoal(LastOfDeepslate boss) {
        this.boss = boss;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override public boolean requiresUpdateEveryTick() { return true; }

    @Override
    public boolean canUse() {
        if (!boss.isCombatReady() || boss.isActionLocked()) return false;
        if (boss.getArchaicSystem().getPhasesTriggered() >= 2) return false;
        return boss.getHealth() / boss.getMaxHealth() <= 0.5F;
    }

    @Override public boolean canContinueToUse() { return timer <= 70 && boss.isAlive(); }

    @Override
    public void start() {
        timer = 0;
        batchIndex = 0;
        smashIndex = 0;
        boss.setActionLocked(true);
        boss.setRaidSpawning(true);
        boss.getNavigation().stop();
        boss.getArchaicSystem().updatePhase();

        int phase = boss.getArchaicSystem().getPhasesTriggered();
        statMultiplier = phase >= 2 ? 1.2F : 1.1F;
        if (phase == 1) {
            regenPerTick = boss.getMaxHealth() / 40.0F;
            regenTicksLeft = 40;
        } else {
            regenPerTick = 0;
            regenTicksLeft = 0;
        }

        int players = Math.max(1, boss.getArchaicSystem().countNearbyPlayers());
        int total = phase >= 2 ? Math.max(5, 5 + 7 * (players - 1)) : Math.max(4, 4 + 6 * (players - 1));
        int base = total / 3;
        int remainder = total % 3;
        batchSizes[0] = base + (remainder >= 1 ? 1 : 0);
        batchSizes[1] = base + (remainder >= 2 ? 1 : 0);
        batchSizes[2] = base;

        int alreadyAlive = boss.getArchaicSystem().countChargedArchaics();
        boss.getArchaicSystem().setArchaicsIntended(Math.max(0, alreadyAlive) + total);
        boss.playSound(ACSounds.LOD_ACTION_START.get(), 3.0F, 1.0F);
        boss.playSound(ACSounds.LOD_WARN_ARCHAICS.get(), 3.0F, 1.0F);
    }

    @Override
    public void tick() {
        timer++;
        boss.getNavigation().stop();
        if (regenTicksLeft > 0) {
            regenTicksLeft--;
            boss.setHealth(Math.min(boss.getMaxHealth(), boss.getHealth() + regenPerTick));
        }
        if (timer >= 45 && smashIndex == 0) { spawnBatchAndSmash(); smashIndex++; }
        else if (timer >= 50 && smashIndex == 1) { spawnBatchAndSmash(); smashIndex++; }
        else if (timer >= 64 && smashIndex == 2) { spawnBatchAndSmash(); smashIndex++; }
    }

    private void spawnBatchAndSmash() {
        spawnArchaicBatch();
        if (!(boss.level() instanceof ServerLevel level)) return;
        boss.playSound(ACSounds.LOD_SMASH.get(), 3.0F, 1.0F);
        com.ratrod.archaion.network.ACNetwork.sendToTrackingPlayers(boss, new com.ratrod.archaion.network.s2c.CameraShakePacket(1.3F, 20, 8.0F));

        // The original does three offset smashes around the boss: +7.5°, -7.5°, then straight ahead.
        float yaw = boss.getYHeadRot() * ((float)Math.PI / 180.0F);
        Vec3 forward = new Vec3(-Math.sin(yaw), 0.0D, Math.cos(yaw));
        double turn = smashIndex == 0 ? Math.toRadians(7.5D) : smashIndex == 1 ? Math.toRadians(-7.5D) : 0.0D;
        double cos = Math.cos(turn);
        double sin = Math.sin(turn);
        Vec3 smashDir = new Vec3(forward.x * cos + forward.z * sin, 0.0D,
                -forward.x * sin + forward.z * cos);
        Vec3 smashPos = boss.position().add(smashDir.scale(7.5D));
        float strength = smashIndex == 2 ? 1.2F : 1.0F;

        AAALevel.addParticle(level, new ParticleEmitterInfo(Archaion.prefix("lod_boom_ground"))
                .position(smashPos.add(0.0D, 0.2D, 0.0D)).scale(3.0F * strength));
        AABB box = AABB.ofSize(smashPos, 12.0D * strength, 6.0D, 12.0D * strength);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, box,
                e -> e != boss && e.isAlive() && boss.canAttack(e))) {
            target.hurt(level.damageSources().mobAttack(boss), boss.attackDamage(strength));
            Vec3 away = target.position().subtract(smashPos);
            if (away.lengthSqr() > 1.0E-6D) {
                away = away.normalize().scale(1.5D * strength).add(0.0D, 0.35D, 0.0D);
                target.setDeltaMovement(target.getDeltaMovement().add(away));
                target.hurtMarked = true;
            }
        }
    }

    private void spawnArchaicBatch() {
        if (!(boss.level() instanceof ServerLevel level) || batchIndex >= batchSizes.length) return;
        int count = batchSizes[batchIndex++];
        for (int i = 0; i < count; i++) spawnArchaic(level);
    }

    private EntityType<? extends Monster> pickArchaicType() {
        boolean phase3 = boss.getArchaicSystem().getPhasesTriggered() >= 2;
        int haunterWeight = phase3 ? 3 : 2;
        int braveWeight = phase3 ? 1 : 0;
        int total = haunterWeight + braveWeight + 3 + 3 + 3;
        int r = boss.getRandom().nextInt(total);
        if ((r -= haunterWeight) < 0) return ACEntityTypes.HAUNTER.get();
        if ((r -= braveWeight) < 0) return ACEntityTypes.BRAVE.get();
        if ((r -= 3) < 0) return ACEntityTypes.WIGHT.get();
        if ((r -= 3) < 0) return ACEntityTypes.SLATED.get();
        return ACEntityTypes.DEEPSLATE_SENTINEL.get();
    }

    private void spawnArchaic(ServerLevel level) {
        Monster mob = pickArchaicType().create(level);
        if (mob == null || !(mob instanceof Archaic archaic)) return;
        Vec3 origin = boss.position();
        for (int attempt = 0; attempt < 20; attempt++) {
            double angle = boss.getRandom().nextDouble() * Math.PI * 2.0D;
            double radius = 4.0D + boss.getRandom().nextDouble() * 7.0D;
            double x = origin.x + Math.cos(angle) * radius;
            double z = origin.z + Math.sin(angle) * radius;
            double y = origin.y;
            mob.moveTo(x, y, z, boss.getRandom().nextFloat() * 360.0F, 0.0F);
            if (!level.noCollision(mob, mob.getBoundingBox())) continue;
            archaic.setCharged(true);
            archaic.setOwnerUUID(boss.getUUID());
            scaleStats(mob);
            level.addFreshEntity(mob);
            AAALevel.addParticle(level, new ParticleEmitterInfo(Archaion.prefix("lod_archaic_summon"))
                    .position(mob.position()).scale(0.2F));
            mob.playSound(ACSounds.LOD_SPAWN_ARCHAICS.get(), 3.0F, 1.0F);
            return;
        }
        mob.discard();
    }

    private void scaleStats(Monster mob) {
        if (statMultiplier <= 1.0F) return;
        for (var attribute : new net.minecraft.world.entity.ai.attributes.Attribute[]{Attributes.MAX_HEALTH, Attributes.ATTACK_DAMAGE, Attributes.ARMOR}) {
            AttributeInstance instance = mob.getAttribute(attribute);
            if (instance != null) instance.setBaseValue(instance.getBaseValue() * statMultiplier);
        }
        mob.setHealth(mob.getMaxHealth());
    }

    @Override
    public void stop() {
        boss.setRaidSpawning(false);
        boss.setActionLocked(false);
    }
}
