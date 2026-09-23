package com.ratrod.archaion.entities.ai.goals;

import com.ratrod.archaion.entities.Grimoray;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

/** Recreates the original orbiting flight and same-species separation behavior. */
public class GrimorayFlightGoal extends Goal {
    private static final double SEPARATION = 3.0D;

    private final Grimoray grimoray;
    private final double speed;
    private final double hoverHeight;
    private final double strafeRadius;
    private double angle;

    public GrimorayFlightGoal(Grimoray grimoray, double speed, double hoverHeight, double strafeRadius) {
        this.grimoray = grimoray;
        this.speed = speed;
        this.hoverHeight = hoverHeight;
        this.strafeRadius = strafeRadius;
        this.angle = grimoray.getRandom().nextFloat() * (Math.PI * 2.0D);
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = grimoray.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void tick() {
        LivingEntity target = grimoray.getTarget();
        if (target == null) return;

        angle += 0.05D;
        double targetX = target.getX() + Math.cos(angle) * strafeRadius;
        double targetZ = target.getZ() + Math.sin(angle) * strafeRadius;
        double targetY = target.getY() + hoverHeight;

        Vec3 toOrbitPoint = new Vec3(targetX, targetY, targetZ).subtract(grimoray.position());
        double length = toOrbitPoint.length();
        Vec3 movement = Vec3.ZERO;
        if (length > 1.0E-4D) movement = toOrbitPoint.scale(Math.min(speed, length) / length);

        List<Grimoray> nearby = grimoray.level().getEntitiesOfClass(Grimoray.class,
                grimoray.getBoundingBox().inflate(SEPARATION), other -> other != grimoray);
        for (Grimoray other : nearby) {
            Vec3 delta = grimoray.position().subtract(other.position());
            double distance = delta.length();
            if (distance > 1.0E-4D && distance < SEPARATION) {
                movement = movement.add(delta.normalize().scale((SEPARATION - distance) / SEPARATION));
            }
        }

        grimoray.setDeltaMovement(movement);
        grimoray.hasImpulse = true;
        grimoray.getLookControl().setLookAt(target, 30.0F, 30.0F);
    }
}
