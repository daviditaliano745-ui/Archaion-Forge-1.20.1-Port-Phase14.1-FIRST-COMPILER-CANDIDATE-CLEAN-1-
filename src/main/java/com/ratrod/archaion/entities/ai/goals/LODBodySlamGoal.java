package com.ratrod.archaion.entities.ai.goals;

import com.ratrod.archaion.Archaion;
import com.ratrod.archaion.entities.LastOfDeepslate;
import com.ratrod.archaion.entities.effect.LODSlamEffect;
import com.ratrod.archaion.registry.ACSounds;
import mod.chloeprime.aaaparticles.api.common.AAALevel;
import mod.chloeprime.aaaparticles.api.common.ParticleEmitterInfo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/** 1.20.1 replacement for LODBodySlamAction. */
public final class LODBodySlamGoal extends Goal {
    private final LastOfDeepslate boss;
    private int timer;
    private int nextUseTick;

    public LODBodySlamGoal(LastOfDeepslate boss) {
        this.boss = boss;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override public boolean requiresUpdateEveryTick() { return true; }
    @Override public boolean canUse() {
        LivingEntity target = boss.getTarget();
        if (!boss.isCombatReady() || boss.isActionLocked() || boss.tickCount < nextUseTick || target == null || !target.isAlive()) return false;
        if (boss.getY() + boss.getBbHeight() < target.getY()) return false;
        float reach = boss.getBbWidth() * (boss.getPhase() >= 2 ? 3.5F : 1.5F);
        return boss.distanceTo(target) <= reach;
    }
    @Override public boolean canContinueToUse() { return timer < 50 && boss.isAlive(); }
    @Override public void start() {
        timer = 0; boss.setActionLocked(true); boss.getNavigation().stop();
        boss.playSound(ACSounds.LOD_ACTION_START.get(), 3.0F, 1.0F);
    }
    @Override public void tick() {
        timer++;
        boss.getNavigation().stop();
        if (timer == 3) {
            boss.moveTo(boss.getX(), boss.getY() + 12.0D, boss.getZ(), boss.getYRot(), boss.getXRot());
            boss.setNoGravity(true);
        }
        if (timer == 25) {
            Vec3 ground = groundPosition();
            boss.moveTo(ground.x, ground.y, ground.z, boss.getYRot(), boss.getXRot());
            boss.setNoGravity(false);
        }
        if (timer == 30) applySlam();
    }

    private Vec3 groundPosition() {
        Vec3 from = boss.position().add(0, 1, 0);
        HitResult hit = boss.level().clip(new ClipContext(from, from.add(0, -100, 0),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, boss));
        return hit.getType() == HitResult.Type.MISS ? boss.position() : hit.getLocation().add(0, 0.1D, 0);
    }

    private void applySlam() {
        if (!(boss.level() instanceof ServerLevel level)) return;
        boss.playSound(ACSounds.LOD_SMASH.get(), 4.0F, 0.8F);
        com.ratrod.archaion.network.ACNetwork.sendToTrackingPlayers(boss, new com.ratrod.archaion.network.s2c.CameraShakePacket(1.5F, 22, 8.0F));
        AAALevel.addParticle(level, new ParticleEmitterInfo(Archaion.prefix("lod_boom_ground"))
                .position(boss.position().add(0, 0.1D, 0)).scale(4.0F));
        if (boss.getPhase() >= 2) LODSlamEffect.summonRing(level, boss);
        Vec3 center = boss.position();
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class,
                net.minecraft.world.phys.AABB.ofSize(center, 16.0D, 10.0D, 16.0D),
                living -> living != boss && living.isAlive() && boss.canAttack(living))) {
            if (!target.hurt(level.damageSources().mobAttack(boss), boss.attackDamage(1.15F))) continue;
            Vec3 push = target.position().subtract(center);
            if (push.lengthSqr() > 1.0E-6D) {
                push = push.normalize().scale(2.0D).add(0, 0.8D, 0);
                target.setDeltaMovement(target.getDeltaMovement().add(push));
                target.hurtMarked = true;
            }
        }
    }

    @Override public void stop() {
        boss.setNoGravity(false);
        boss.setActionLocked(false);
        nextUseTick = boss.tickCount + 100;
    }
}
