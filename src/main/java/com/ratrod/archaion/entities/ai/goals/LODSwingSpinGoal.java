package com.ratrod.archaion.entities.ai.goals;

import com.ratrod.archaion.Archaion;
import com.ratrod.archaion.entities.LastOfDeepslate;
import com.ratrod.archaion.registry.ACSounds;
import mod.chloeprime.aaaparticles.api.common.AAALevel;
import mod.chloeprime.aaaparticles.api.common.ParticleEmitterInfo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/** 1.20.1 replacement for LODSwingSpinAction. */
public final class LODSwingSpinGoal extends Goal {
    private final LastOfDeepslate boss;
    private int timer;
    private int nextUseTick;

    public LODSwingSpinGoal(LastOfDeepslate boss) {
        this.boss = boss;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override public boolean requiresUpdateEveryTick() { return true; }
    @Override public boolean canUse() {
        LivingEntity target = boss.getTarget();
        return boss.isCombatReady() && !boss.isActionLocked() && boss.tickCount >= nextUseTick
                && target != null && target.isAlive() && boss.hasLineOfSight(target)
                && boss.distanceTo(target) <= boss.getBbWidth() * 1.25F;
    }
    @Override public boolean canContinueToUse() { return timer < 50 && boss.isAlive(); }
    @Override public void start() {
        timer = 0; boss.setActionLocked(true); boss.getNavigation().stop();
        boss.playSound(ACSounds.LOD_ACTION_START.get(), 3.0F, 1.0F);
    }
    @Override public void tick() {
        timer++;
        boss.getNavigation().stop();
        if (timer == 10) {
            boss.playSound(ACSounds.LOD_SPIN.get(), 3.0F, 1.0F);
            AAALevel.addParticle(boss.level(), new ParticleEmitterInfo(Archaion.prefix("echo_spin"))
                    .position(boss.position().add(0, 1, 0)).scale(2.0F));
        }
        if (timer == 15 && boss.level() instanceof ServerLevel level) {
            Vec3 center = boss.position();
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, boss.getBoundingBox().inflate(2.0D),
                    living -> living != boss && living.isAlive() && boss.canAttack(living))) {
                target.hurt(level.damageSources().mobAttack(boss), boss.attackDamage(0.9F));
                Vec3 push = target.position().subtract(center);
                if (push.lengthSqr() > 1.0E-6D) {
                    push = push.normalize().scale(1.5D).add(0, 0.5D, 0);
                    target.setDeltaMovement(target.getDeltaMovement().add(push));
                    target.hurtMarked = true;
                }
            }
        }
    }
    @Override public void stop() {
        boss.setActionLocked(false);
        nextUseTick = boss.tickCount + 150;
    }
}
