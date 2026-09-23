package com.ratrod.archaion.entities.ai.goals;

import com.ratrod.archaion.Archaion;
import com.ratrod.archaion.entities.LastOfDeepslate;
import com.ratrod.archaion.entities.effect.LODFallingBlock;
import com.ratrod.archaion.registry.ACSounds;
import mod.chloeprime.aaaparticles.api.common.AAALevel;
import mod.chloeprime.aaaparticles.api.common.ParticleEmitterInfo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/** 1.20.1 replacement for LODSmashGroundAction. */
public final class LODSmashGroundGoal extends Goal {
    private final LastOfDeepslate boss;
    private int timer;
    private int nextUseTick;

    public LODSmashGroundGoal(LastOfDeepslate boss) {
        this.boss = boss;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override public boolean requiresUpdateEveryTick() { return true; }
    @Override public boolean canUse() {
        LivingEntity target = boss.getTarget();
        if (!boss.isCombatReady() || boss.isActionLocked() || boss.tickCount < nextUseTick || target == null || !target.isAlive()) return false;
        if (boss.getY() + 3.0D < target.getY()) return false;
        float factor = boss.getPhase() >= 1 ? 8.0F : 1.5F;
        return boss.distanceTo(target) <= boss.getBbWidth() * factor;
    }
    @Override public boolean canContinueToUse() {
        int duration = boss.level().getDifficulty() == Difficulty.HARD ? 50 : 60;
        return timer < duration && boss.isAlive();
    }
    @Override public void start() {
        timer = 0; boss.setActionLocked(true); boss.setHeadTrackingSuppressed(true); boss.getNavigation().stop();
        boss.playSound(ACSounds.LOD_ACTION_START.get(), 3.0F, 1.0F);
    }
    @Override public void tick() {
        timer++;
        boss.getNavigation().stop();
        LivingEntity target = boss.getTarget();
        if (timer > 18 && target != null) boss.getLookControl().setLookAt(target, 30.0F, 30.0F);
        if (timer == 28) boss.setHeadTrackingSuppressed(false);
        if (timer == 27) applySmash(boss.level().getDifficulty() == Difficulty.HARD ? 1.075F : 1.0F);
    }

    private void applySmash(float multiplier) {
        if (!(boss.level() instanceof ServerLevel level)) return;
        boss.playSound(ACSounds.LOD_SMASH.get(), 3.0F, 1.0F);
        com.ratrod.archaion.network.ACNetwork.sendToTrackingPlayers(boss, new com.ratrod.archaion.network.s2c.CameraShakePacket(1.1F, 18, 8.0F));
        if (boss.getPhase() >= 1) raiseBlocks(level);

        float yaw = boss.getYHeadRot() * ((float)Math.PI / 180.0F);
        Vec3 direction = new Vec3(-Mth.sin(yaw), 0, Mth.cos(yaw));
        Vec3 center = boss.position().add(direction.yRot(0.1308997F).scale(7.5D));
        AAALevel.addParticle(level, new ParticleEmitterInfo(Archaion.prefix("lod_boom_ground"))
                .position(center.add(0, 0.2D, 0)).scale(3.0F));
        AABB box = AABB.ofSize(center, 9.0D, 6.0D, 9.0D);
        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, box,
                target -> target != boss && target.isAlive() && boss.canAttack(target))) {
            if (!living.hurt(level.damageSources().mobAttack(boss), boss.attackDamage(multiplier))) continue;
            Vec3 push = living.position().subtract(center);
            if (push.lengthSqr() > 1.0E-6D) {
                push = push.normalize().scale(1.5D).add(0, 0.35D, 0);
                living.setDeltaMovement(living.getDeltaMovement().add(push));
                living.hurtMarked = true;
            }
        }
    }

    /** Uses heightmap lookup instead of the original 64-block per-column scan; same visible result at far lower server cost. */
    private void raiseBlocks(ServerLevel level) {
        int baseX = boss.blockPosition().getX();
        int baseZ = boss.blockPosition().getZ();
        for (int dx = -32; dx <= 32; dx++) {
            for (int dz = -32; dz <= 32; dz++) {
                if (boss.getRandom().nextFloat() >= 0.01F) continue;
                int x = baseX + dx, z = baseZ + dz;
                int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1;
                var pos = new net.minecraft.core.BlockPos(x, y, z);
                var state = level.getBlockState(pos);
                if (!state.isAir() && !state.hasBlockEntity()) LODFallingBlock.spawn(level, state, Vec3.atCenterOf(pos), boss);
            }
        }
    }

    @Override public void stop() {
        boss.setActionLocked(false);
        boss.setHeadTrackingSuppressed(false);
        nextUseTick = boss.tickCount + 100;
    }
}
