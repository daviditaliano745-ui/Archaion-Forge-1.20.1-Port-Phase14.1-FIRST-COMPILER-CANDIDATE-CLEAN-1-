package com.ratrod.archaion.entities.ai.controls.look;

import com.ratrod.archaion.entities.LastOfDeepslate;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.phys.Vec3;

/**
 * 1.20.1 backport of the original Last of Deepslate eased look control.
 *
 * The 1.21 implementation froze this controller during the first 27 ticks of the
 * Smash Ground ActionManager action. The native-goal port exposes an equivalent
 * suppression flag so the same presentation survives without restoring ActionManager.
 */
public class LastOfDeepslateLookControl extends LookControl {
    public LastOfDeepslateLookControl(Mob mob) {
        super(mob);
    }

    @Override
    public void tick() {
        LivingEntity target = mob.getTarget();
        if (target != null && target.isAlive()) {
            if (mob instanceof LastOfDeepslate boss && boss.isHeadTrackingSuppressed()) {
                return;
            }

            Vec3 direction = target.getEyePosition().subtract(mob.getEyePosition()).normalize();
            float targetYaw = (float) (Mth.atan2(-direction.x, direction.z) * (180.0D / Math.PI));
            float targetPitch = (float) -(Mth.atan2(direction.y, Math.hypot(direction.x, direction.z)) * (180.0D / Math.PI));

            float easedYaw = Mth.rotLerp(0.4F, mob.yHeadRot, targetYaw);
            mob.yHeadRot += Mth.clamp(Mth.degreesDifference(mob.yHeadRot, easedYaw), -60.0F, 60.0F);
            mob.setYRot(mob.getYHeadRot());

            float easedPitch = Mth.rotLerp(0.25F, mob.getXRot(), targetPitch);
            mob.setXRot(mob.getXRot() + Mth.clamp(Mth.degreesDifference(mob.getXRot(), easedPitch), -40.0F, 40.0F));
        } else {
            mob.yHeadRot += Mth.clamp(Mth.degreesDifference(mob.yHeadRot, mob.yBodyRot), -40.0F, 40.0F);
        }
    }
}
