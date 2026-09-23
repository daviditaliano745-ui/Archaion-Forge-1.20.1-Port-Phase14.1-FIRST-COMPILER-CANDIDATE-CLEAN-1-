package com.ratrod.archaion.entities.ai.goals;

import com.ratrod.archaion.entities.DeepslateSentinel;
import com.ratrod.archaion.entities.Wight;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.EnumSet;

/** Forge 1.20.1 port of Archaion's Wight pickup goal. */
public class PickUpRidersGoal extends Goal {
    private final DeepslateSentinel sentinel;
    private final double speedModifier;
    private final double searchRadius;
    private Entity target;
    private int pathRecalcTicks;

    public PickUpRidersGoal(DeepslateSentinel sentinel, double speedModifier, double searchRadius) {
        this.sentinel = sentinel;
        this.speedModifier = speedModifier;
        this.searchRadius = searchRadius;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public boolean canUse() {
        target = findPickupTarget();
        return target != null;
    }

    @Override
    public boolean canContinueToUse() {
        return target != null && target.isAlive() && !target.isPassenger()
                && sentinel.getPassengers().isEmpty();
    }

    @Override
    public void start() {
        pathRecalcTicks = 0;
    }

    @Override
    public void stop() {
        target = null;
        sentinel.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (target == null) return;

        if (--pathRecalcTicks <= 0) {
            pathRecalcTicks = 5;
            Vec3 pos = target.position();
            sentinel.getNavigation().moveTo(pos.x, pos.y, pos.z, speedModifier);
        }

        if (sentinel.distanceToSqr(target) < 6.0D) {
            target.startRiding(sentinel);
        }
    }

    private Entity findPickupTarget() {
        if (!sentinel.getPassengers().isEmpty()) return null;
        AABB area = sentinel.getBoundingBox().inflate(searchRadius);
        return sentinel.level().getEntitiesOfClass(Wight.class, area,
                        wight -> wight.isAlive() && !wight.isPassenger() && sentinel.hasLineOfSight(wight))
                .stream()
                .min(Comparator.comparingDouble(sentinel::distanceToSqr))
                .orElse(null);
    }
}
