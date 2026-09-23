package com.ratrod.archaion.entities.ai.goals;

import com.ratrod.archaion.Archaion;
import com.ratrod.archaion.entities.DeepslateSentinel;
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
import java.util.List;

/** Native 1.20.1 replacement for SentinelChargeAction. */
public class SentinelChargeGoal extends Goal {
    private final DeepslateSentinel sentinel;
    private Vec3 chargeDir = Vec3.ZERO;
    private Vec3 lastPos = Vec3.ZERO;
    private int stuckTicks;
    private int timer;

    public SentinelChargeGoal(DeepslateSentinel sentinel) {
        this.sentinel = sentinel;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public boolean canUse() {
        if (sentinel.isChargeOnCooldown()) return false;
        LivingEntity target = sentinel.getTarget();
        if (target == null || !target.isAlive()) return false;
        if (!sentinel.hasLineOfSight(target)) return false;
        if (!sentinel.getPassengers().isEmpty()) return false;
        return !sentinel.hasNearbyRidersToPickup(32.0D);
    }

    @Override
    public boolean canContinueToUse() {
        return timer < 140;
    }

    @Override
    public void start() {
        timer = 0;
        stuckTicks = 0;
        lastPos = sentinel.position();
        sentinel.getNavigation().stop();

        LivingEntity target = sentinel.getTarget();
        Vec3 dir = target != null
                ? target.position().subtract(sentinel.position())
                : directionFromYaw(sentinel.getYRot());
        dir = new Vec3(dir.x, 0.0D, dir.z);
        chargeDir = dir.lengthSqr() < 1.0E-6D ? directionFromYaw(sentinel.getYRot()) : dir.normalize();

        sentinel.playSound(ACSounds.SENTINEL_START_CHARGING.get(), 2.0F, 1.0F);
    }

    @Override
    public void tick() {
        timer++;
        boolean windup = timer <= 30;
        boolean charging = timer > 30 && timer <= 120;

        float yaw = yawOf(chargeDir);
        sentinel.setYRot(yaw);
        sentinel.yBodyRot = yaw;

        LivingEntity target = sentinel.getTarget();
        if (windup && target != null && target.isAlive()) {
            sentinel.getLookControl().setLookAt(target, 45.0F, 45.0F);
        }

        if (charging) {
            if (isStuck()) {
                timer = 140;
                return;
            }

            Vec3 current = sentinel.getDeltaMovement();
            Vec3 motion = chargeDir.scale(chargeSpeed()).add(0.0D, current.y, 0.0D);
            sentinel.setDeltaMovement(motion);
            sentinel.hasImpulse = true;

            if (timer % 4 == 0) applyBoom();
            if (timer % 2 == 0) {
                sentinel.playSound(ACSounds.ECHO_STAR_BLAST.get(), 1.0F,
                        1.4F + sentinel.getRandom().nextFloat() * 0.2F);
            }
        } else {
            Vec3 current = sentinel.getDeltaMovement();
            sentinel.setDeltaMovement(0.0D, current.y, 0.0D);
        }
    }

    @Override
    public void stop() {
        Vec3 current = sentinel.getDeltaMovement();
        sentinel.setDeltaMovement(0.0D, current.y, 0.0D);
        sentinel.startChargeCooldown();
    }

    private void applyBoom() {
        if (!(sentinel.level() instanceof ServerLevel level)) return;

        ParticleEmitterInfo particle = new ParticleEmitterInfo(Archaion.prefix("echo_blast_intercept"))
                .position(sentinel.position().offsetRandom(level.getRandom(), 0.5F))
                .rotation(0.0F, level.getRandom().nextFloat() * 90.0F, 0.0F)
                .scale(1.4F);
        AAALevel.addParticle(level, particle);

        double width = sentinel.getBbWidth() + 1.0F;
        AABB area = AABB.ofSize(sentinel.position(), width, 3.0D, width);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area,
                living -> living != sentinel && living.isAlive() && sentinel.canAttack(living));

        for (LivingEntity living : targets) {
            if (!living.hurt(level.damageSources().explosion(sentinel, sentinel), 15.0F)) continue;
            Vec3 push = living.position().subtract(sentinel.position());
            if (push.lengthSqr() < 1.0E-6D) continue;
            push = push.normalize().scale(3.0D).add(0.0D, 0.4D, 0.0D);
            living.setDeltaMovement(living.getDeltaMovement().add(push));
            living.hurtMarked = true;
        }
    }

    private double chargeSpeed() {
        float accel = Mth.clamp((timer - 30) / 6.0F, 0.0F, 1.0F);
        float decel = timer > 115 ? (120 - timer) / 5.0F : 1.0F;
        return 1.1F * accel * Mth.clamp(decel, 0.0F, 1.0F);
    }

    private boolean isStuck() {
        double dx = sentinel.getX() - lastPos.x;
        double dz = sentinel.getZ() - lastPos.z;
        lastPos = sentinel.position();
        if (dx * dx + dz * dz < 0.01D) {
            return ++stuckTicks > 8;
        }
        stuckTicks = 0;
        return false;
    }

    private static float yawOf(Vec3 vec) {
        return (float) (Mth.atan2(-vec.x, vec.z) * (180.0D / Math.PI));
    }

    private static Vec3 directionFromYaw(float yaw) {
        float radians = yaw * ((float) Math.PI / 180.0F);
        return new Vec3(-Mth.sin(radians), 0.0D, Mth.cos(radians));
    }
}
