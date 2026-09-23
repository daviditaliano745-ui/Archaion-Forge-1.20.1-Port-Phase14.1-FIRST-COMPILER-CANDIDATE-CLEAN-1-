package com.ratrod.archaion.entities.ai.goals;

import com.ratrod.archaion.Archaion;
import com.ratrod.archaion.entities.Haunter;
import com.ratrod.archaion.registry.ACEffects;
import com.ratrod.archaion.registry.ACSounds;
import mod.chloeprime.aaaparticles.api.common.AAALevel;
import mod.chloeprime.aaaparticles.api.common.ParticleEmitterInfo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

/** Native Forge replacement for the original HaunterExplodeAction. */
public final class HaunterExplodeGoal extends Goal {
    private final Haunter haunter;
    private int timer;

    public HaunterExplodeGoal(Haunter haunter) {
        this.haunter = haunter;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override public boolean requiresUpdateEveryTick() { return true; }

    @Override
    public boolean canUse() {
        LivingEntity target = haunter.getTarget();
        return haunter.explodingCooldown <= 0
                && target != null && target.isAlive()
                && haunter.distanceTo(target) <= 3.5F;
    }

    @Override public boolean canContinueToUse() { return timer <= 60; }

    @Override
    public void start() {
        timer = 0;
        haunter.setSwelling(true);
        haunter.getNavigation().stop();
        haunter.playSound(SoundEvents.CREEPER_PRIMED, 1.0F, 0.2F);
    }

    @Override
    public void tick() {
        timer++;
        haunter.getNavigation().stop();
        LivingEntity target = haunter.getTarget();
        if (target != null) haunter.getLookControl().setLookAt(target, 45.0F, 45.0F);
        if (timer == 40) explode();
    }

    private void explode() {
        haunter.setSwelling(false);
        if (!(haunter.level() instanceof ServerLevel level)) return;

        haunter.playSound(ACSounds.HAUNTER_EXPLODE.get(), 3.0F, 1.0F);
        com.ratrod.archaion.network.ACNetwork.sendToTrackingPlayers(haunter, new com.ratrod.archaion.network.s2c.CameraShakePacket(0.45F, 10, 9.0F));
        AAALevel.addParticle(level, new ParticleEmitterInfo(Archaion.prefix("haunter_boom"))
                .position(haunter.position().add(0.0D, 1.0D, 0.0D)).scale(1.5F));

        AABB area = haunter.getBoundingBox().inflate(3.0D, 0.0D, 3.0D);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area,
                living -> living != haunter && living.isAlive() && haunter.canAttack(living));
        for (LivingEntity living : targets) {
            if (!living.hurt(level.damageSources().explosion(haunter, haunter), 15.0F)) continue;
            living.addEffect(new MobEffectInstance(ACEffects.ARMOR_BREAK.get(), 200, 1), haunter);
            Vec3 push = living.position().subtract(haunter.position());
            push = new Vec3(push.x, 0.0D, push.z);
            if (push.lengthSqr() > 1.0E-6D) {
                push = push.normalize().scale(2.0D).add(0.0D, 0.35D, 0.0D);
                living.setDeltaMovement(living.getDeltaMovement().add(push));
                living.hurtMarked = true;
            }
        }
    }

    @Override
    public void stop() {
        haunter.setSwelling(false);
        haunter.explodingCooldown = 100 + haunter.getRandom().nextInt(61);
    }
}
