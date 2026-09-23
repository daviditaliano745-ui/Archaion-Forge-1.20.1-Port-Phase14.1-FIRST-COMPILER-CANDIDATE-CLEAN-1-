package com.ratrod.archaion.entities.ai.goals;

import com.ratrod.archaion.Archaion;
import com.ratrod.archaion.entities.LastOfDeepslate;
import com.ratrod.archaion.registry.ACSounds;
import mod.chloeprime.aaaparticles.api.common.AAALevel;
import mod.chloeprime.aaaparticles.api.common.ParticleEmitterInfo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/** Forge 1.20.1 translation of Archaion's LODRollAction. */
public final class LODRollGoal extends Goal {
    private final LastOfDeepslate boss;
    private LivingEntity lockedTarget;
    private Vec3 rollDir = Vec3.ZERO;
    private Vec3 lastPos = Vec3.ZERO;
    private int timer;
    private int nextUseTick;
    private int stuckTicks;
    private int escapeSide = 1;

    public LODRollGoal(LastOfDeepslate boss) {
        this.boss = boss;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override public boolean requiresUpdateEveryTick() { return true; }

    @Override
    public boolean canUse() {
        LivingEntity target = boss.getTarget();
        if (!boss.isCombatReady() || boss.isActionLocked() || boss.getPhase() < 1 || boss.tickCount < nextUseTick) return false;
        if (target == null || !target.isAlive()) return false;
        if (boss.getY() + boss.getBbHeight() < target.getY()) return false;
        // The original action is chosen from a weighted action list. A gated random roll
        // keeps it from monopolising Forge's priority goal selector while preserving its frequency.
        return boss.getRandom().nextInt(boss.getPhase() >= 2 ? 36 : 52) == 0;
    }

    @Override public boolean canContinueToUse() { return timer < 140 && lockedTarget != null && lockedTarget.isAlive() && boss.isAlive(); }

    @Override
    public void start() {
        timer = 0;
        stuckTicks = 0;
        escapeSide = 1;
        lockedTarget = boss.getTarget();
        rollDir = directionFromYaw(boss.getYRot());
        lastPos = boss.position();
        boss.setActionLocked(true);
        boss.getNavigation().stop();
        boss.playSound(ACSounds.LOD_ACTION_START.get(), 3.0F, 1.0F);
    }

    @Override
    public void tick() {
        timer++;
        boss.getNavigation().stop();
        if (lockedTarget == null || !lockedTarget.isAlive()) return;

        boolean rolling = timer > 32 && timer < 120;
        if (rolling) {
            steerHeading(lockedTarget);
            if (isStuck()) rollDir = directionFromYaw(yawOf(rollDir) + escapeSide * 30.0F);
            if ((timer & 3) == 0) applyBoom();
            if ((timer & 1) == 0) boss.playSound(ACSounds.ECHO_STAR_BLAST.get(), 2.0F, 0.6F + boss.getRandom().nextFloat() * 0.2F);
        }
        applyRotation();

        double y = boss.getDeltaMovement().y;
        if (rolling) boss.setDeltaMovement(rollDir.scale(rollSpeed()).add(0, y, 0));
        else boss.setDeltaMovement(0, y, 0);
    }

    private void steerHeading(LivingEntity target) {
        Vec3 desired = target.position().subtract(boss.position()).multiply(1, 0, 1);
        if (desired.lengthSqr() < 1.0E-6D) return;
        float currentYaw = yawOf(rollDir);
        float targetYaw = yawOf(desired.normalize());
        float maxTurn = 0.4F * 1.75F * 20.0F; // original steering constants, expressed per game tick
        float delta = Mth.clamp(Mth.degreesDifference(currentYaw, targetYaw), -maxTurn, maxTurn);
        rollDir = directionFromYaw(currentYaw + delta);
    }

    private boolean isStuck() {
        double dx = boss.getX() - lastPos.x;
        double dz = boss.getZ() - lastPos.z;
        lastPos = boss.position();
        if (timer > 40 && timer <= 115 && dx * dx + dz * dz < 9.0E-4D) {
            stuckTicks++;
            if (stuckTicks % 8 == 0) escapeSide = -escapeSide;
            return true;
        }
        stuckTicks = 0;
        return false;
    }

    private void applyBoom() {
        if (!(boss.level() instanceof ServerLevel level)) return;
        AAALevel.addParticle(level, new ParticleEmitterInfo(Archaion.prefix("echo_blast_intercept"))
                .position(boss.position().offsetRandom(level.random, 0.5F)).scale(2.0F));
        double w = boss.getBbWidth();
        AABB area = AABB.ofSize(boss.position(), w, 3.0D, w);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area,
                e -> e != boss && e.isAlive() && boss.canAttack(e))) {
            if (!target.hurt(level.damageSources().mobAttack(boss), boss.attackDamage(0.6F))) continue;
            Vec3 push = target.position().subtract(boss.position());
            if (push.lengthSqr() > 1.0E-6D) {
                push = push.normalize().scale(3.0D).add(0, 0.35D, 0);
                target.setDeltaMovement(target.getDeltaMovement().add(push));
                target.hurtMarked = true;
            }
        }
    }

    private double rollSpeed() {
        float accel = Mth.clamp((timer - 30) / 8.0F, 0.0F, 1.0F);
        float brake = timer > 115 ? (140 - timer) / 25.0F : 1.0F;
        return 1.2D * accel * Mth.clamp(brake, 0.0F, 1.0F);
    }

    private void applyRotation() {
        float yaw = yawOf(rollDir);
        boss.setYRot(yaw);
        boss.yBodyRot = yaw;
        boss.setXRot(Mth.rotLerp(0.3F, boss.getXRot(), 0));
    }

    private static Vec3 directionFromYaw(float yaw) {
        float rad = yaw * ((float)Math.PI / 180.0F);
        return new Vec3(-Mth.sin(rad), 0, Mth.cos(rad));
    }

    private static float yawOf(Vec3 dir) {
        return (float)(Mth.atan2(-dir.x, dir.z) * (180.0D / Math.PI));
    }

    @Override
    public void stop() {
        lockedTarget = null;
        boss.setActionLocked(false);
        boss.setDeltaMovement(0, boss.getDeltaMovement().y, 0);
        nextUseTick = boss.tickCount + (boss.getPhase() >= 2 ? 70 : 110);
    }
}
