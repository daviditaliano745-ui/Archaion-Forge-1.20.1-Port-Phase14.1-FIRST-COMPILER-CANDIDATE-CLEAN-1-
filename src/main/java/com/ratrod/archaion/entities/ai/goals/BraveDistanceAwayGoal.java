package com.ratrod.archaion.entities.ai.goals;

import com.ratrod.archaion.entities.Brave;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/** Keeps Brave at its original 18-block preferred spacing when a target is too close. */
public class BraveDistanceAwayGoal extends Goal {
    private final Brave brave;
    private final double speedModifier;
    private final double desiredDistance;
    private int recalcTicks;

    public BraveDistanceAwayGoal(Brave brave, double speedModifier, double desiredDistance) {
        this.brave = brave;
        this.speedModifier = speedModifier;
        this.desiredDistance = desiredDistance;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = brave.getTarget();
        return target != null && target.isAlive() && brave.mustRetreat(target.position());
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = brave.getTarget();
        return target != null && target.isAlive() && brave.distanceToSqr(target) < desiredDistance * desiredDistance;
    }

    @Override
    public void start() {
        recalcTicks = 0;
    }

    @Override
    public void stop() {
        brave.getNavigation().stop();
    }

    @Override
    public void tick() {
        LivingEntity target = brave.getTarget();
        if (target == null) return;
        brave.getLookControl().setLookAt(target, 30.0F, 30.0F);

        if (--recalcTicks > 0) return;
        recalcTicks = 8 + brave.getRandom().nextInt(5);

        Vec3 away = brave.position().subtract(target.position());
        Vec3 horizontal = new Vec3(away.x, 0.0D, away.z);
        if (horizontal.lengthSqr() < 1.0E-6D) {
            horizontal = new Vec3(brave.getRandom().nextDouble() - 0.5D, 0.0D,
                    brave.getRandom().nextDouble() - 0.5D);
        }
        horizontal = horizontal.normalize().scale(desiredDistance);
        Vec3 destination = target.position().add(horizontal.x, 0.0D, horizontal.z);
        brave.getNavigation().moveTo(destination.x, brave.getY(), destination.z, speedModifier);
    }
}
