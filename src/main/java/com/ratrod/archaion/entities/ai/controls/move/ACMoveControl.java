package com.ratrod.archaion.entities.ai.controls.move;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.MoveControl;

/**
 * 1.20.1 backport of Archaion's turn-aware movement control.
 *
 * The original ACEntity interface supplied a per-entity rotation-freedom value. The
 * ActionManager-facing parts of that interface are intentionally not brought back;
 * this control accepts the same value directly instead.
 */
public class ACMoveControl extends MoveControl {
    private final float rotationFreedom;

    public ACMoveControl(Mob mob) {
        this(mob, 1.0F);
    }

    public ACMoveControl(Mob mob, float rotationFreedom) {
        super(mob);
        this.rotationFreedom = rotationFreedom;
    }

    @Override
    protected float rotlerp(float sourceAngle, float targetAngle, float maximumChange) {
        return super.rotlerp(sourceAngle, targetAngle, maximumChange * rotationFreedom);
    }

    @Override
    public void tick() {
        if (operation == Operation.MOVE_TO) {
            double dx = wantedX - mob.getX();
            double dz = wantedZ - mob.getZ();
            float wantedYaw = (float) (Mth.atan2(dz, dx) * (180.0D / Math.PI)) - 90.0F;
            float angleDifference = Math.abs(Mth.wrapDegrees(wantedYaw - mob.getYRot()));
            float steeringSpeedFactor = Math.max(0.25F, 1.0F - angleDifference / 90.0F);

            double originalSpeedModifier = speedModifier;
            speedModifier *= steeringSpeedFactor;
            super.tick();
            speedModifier = originalSpeedModifier;
        } else {
            super.tick();
        }
    }
}
