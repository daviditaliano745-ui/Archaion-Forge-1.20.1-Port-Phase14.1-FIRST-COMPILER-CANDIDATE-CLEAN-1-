package com.ratrod.archaion.entities.ai.goals;

import com.ratrod.archaion.entities.Brave;
import com.ratrod.archaion.registry.ACSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

/** Native 1.20.1 replacement for BraveJumpOnAction. */
public class BraveLeapGoal extends Goal {
    private final Brave brave;
    private int timer;
    private boolean launched;
    private boolean landed;

    public BraveLeapGoal(Brave brave) {
        this.brave = brave;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = brave.getTarget();
        return target != null && target.isAlive() && brave.getSensing().hasLineOfSight(target)
                && !brave.mustRetreat(target.position());
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = brave.getTarget();
        return target != null && target.isAlive() && timer <= 80 && !landed;
    }

    @Override
    public void start() {
        timer = 0;
        launched = false;
        landed = false;
        brave.getNavigation().stop();
    }

    @Override
    public void tick() {
        LivingEntity target = brave.getTarget();
        if (target == null) return;
        timer++;
        brave.getLookControl().setLookAt(target, 30.0F, 30.0F);

        if (timer == 15) {
            brave.playSound(ACSounds.BRAVE_JUMP.get(), 3.0F, 1.0F);
            launchToward(target);
            launched = true;
        }

        if (launched && timer >= 30 && brave.onGround()) {
            Vec3 motion = brave.getDeltaMovement();
            brave.setDeltaMovement(motion.x * 0.1D, motion.y, motion.z * 0.1D);
            applyLandingDamage();
            landed = true;
        }
    }

    private void launchToward(LivingEntity target) {
        Vec3 delta = target.position().subtract(brave.position());
        double horizontalDistance = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        if (horizontalDistance < 1.0E-4D) return;

        double horizontalSpeed = Mth.clamp(horizontalDistance * 0.08D, 0.5D, 1.1D);
        brave.setDeltaMovement(delta.x / horizontalDistance * horizontalSpeed,
                0.75D,
                delta.z / horizontalDistance * horizontalSpeed);
        brave.hasImpulse = true;
    }

    private void applyLandingDamage() {
        if (!(brave.level() instanceof ServerLevel serverLevel)) return;

        Vec3 center = brave.position();
        AABB hitbox = AABB.ofSize(center, 8.0D, 6.0D, 8.0D);
        List<LivingEntity> targets = serverLevel.getEntitiesOfClass(LivingEntity.class, hitbox,
                entity -> entity != brave && entity.isAlive() && brave.canAttack(entity));

        // 1.20.1 has no mace-smash sound/event. This is the closest vanilla feedback.
        brave.playSound(SoundEvents.GENERIC_EXPLODE, 2.0F, 1.1F);
        serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.POOF,
                center.x, center.y + 0.15D, center.z, 30, 1.5D, 0.25D, 1.5D, 0.08D);

        float damage = (float) brave.getAttributeValue(Attributes.ATTACK_DAMAGE);
        for (LivingEntity entity : targets) {
            if (!entity.hurt(brave.damageSources().mobAttack(brave), damage)) continue;
            Vec3 knockback = entity.position().subtract(center);
            if (knockback.lengthSqr() > 1.0E-6D) {
                knockback = knockback.normalize().scale(1.8D);
                entity.setDeltaMovement(entity.getDeltaMovement().add(knockback));
                entity.hurtMarked = true;
            }
        }
    }
}
