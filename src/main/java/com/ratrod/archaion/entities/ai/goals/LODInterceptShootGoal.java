package com.ratrod.archaion.entities.ai.goals;

import com.ratrod.archaion.entities.LastOfDeepslate;
import com.ratrod.archaion.entities.projectile.EchoStarProjectile;
import com.ratrod.archaion.entities.projectile.LODInterceptBlast;
import com.ratrod.archaion.registry.ACEntityTypes;
import com.ratrod.archaion.registry.ACSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/** Anti-air attack retained from LODInterceptShootAction. */
public final class LODInterceptShootGoal extends Goal {
    private final LastOfDeepslate boss;
    private int timer;
    private int nextUseTick;

    public LODInterceptShootGoal(LastOfDeepslate boss) {
        this.boss = boss;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override public boolean requiresUpdateEveryTick() { return true; }
    @Override public boolean canUse() {
        LivingEntity target = boss.getTarget();
        return boss.isCombatReady() && !boss.isActionLocked() && boss.tickCount >= nextUseTick
                && target != null && target.isAlive() && boss.hasLineOfSight(target)
                && boss.getY() + boss.getBbHeight() < target.getY();
    }
    @Override public boolean canContinueToUse() { return timer < 40 && boss.isAlive(); }

    @Override public void start() {
        timer = 0;
        boss.setActionLocked(true);
        boss.getNavigation().stop();
        boss.playSound(ACSounds.LOD_ACTION_START.get(), 3.0F, 1.0F);
    }

    @Override public void tick() {
        timer++;
        boss.getNavigation().stop();
        LivingEntity target = boss.getTarget();
        if (target != null) boss.getLookControl().setLookAt(target, 30.0F, 30.0F);
        if (timer != 14 || !(boss.level() instanceof ServerLevel level)) return;

        float yaw = boss.getYHeadRot() * ((float)Math.PI / 180.0F);
        Vec3 side = new Vec3(-Mth.sin(yaw), 0, Mth.cos(yaw)).yRot(-1.5707964F).scale(3.0D);
        Vec3 muzzle = boss.position().add(side).add(0, boss.getBbHeight() + 6.0D, 0);

        LODInterceptBlast blast = ACEntityTypes.LOD_INTERCEPT_BLAST.get().create(level);
        if (blast != null) {
            blast.moveTo(muzzle.x, muzzle.y, muzzle.z, boss.getYRot(), 0);
            blast.setOwner(boss);
            blast.setDeltaMovement(0, 3.0D, 0);
            level.addFreshEntity(blast);
        }

        for (int i = 0; i < 5; i++) {
            EchoStarProjectile star = ACEntityTypes.ECHO_STAR.get().create(level);
            if (star == null) continue;
            star.moveTo(muzzle.x, muzzle.y, muzzle.z, boss.getYRot(), 0);
            star.setOwner(boss);
            star.setDeltaMovement((boss.getRandom().nextDouble() - 0.5D) * 2.0D,
                    1.0D,
                    (boss.getRandom().nextDouble() - 0.5D) * 2.0D);
            level.addFreshEntity(star);
        }
    }

    @Override public void stop() {
        boss.setActionLocked(false);
        nextUseTick = boss.tickCount + 500;
    }
}
