package com.ratrod.archaion.entities.ai.goals;

import com.ratrod.archaion.Archaion;
import com.ratrod.archaion.entities.LastOfDeepslate;
import com.ratrod.archaion.entities.projectile.EchoStarProjectile;
import com.ratrod.archaion.entities.projectile.LODInterceptBlast;
import com.ratrod.archaion.registry.ACEntityTypes;
import com.ratrod.archaion.registry.ACSounds;
import mod.chloeprime.aaaparticles.api.common.AAALevel;
import mod.chloeprime.aaaparticles.api.common.ParticleEmitterInfo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/** Normal ranged action: either one moving blast or a 3-volley Echo Star spread. */
public final class LODShootGoal extends Goal {
    private final LastOfDeepslate boss;
    private int timer;
    private boolean blast;

    public LODShootGoal(LastOfDeepslate boss) {
        this.boss = boss;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override public boolean requiresUpdateEveryTick() { return true; }
    @Override public boolean canUse() {
        LivingEntity target = boss.getTarget();
        if (!boss.isCombatReady() || boss.isActionLocked() || boss.shootingCooldown > 0 || target == null || !target.isAlive()) return false;
        if (!boss.hasLineOfSight(target)) return false;
        if (boss.getY() + boss.getBbHeight() < target.getY()) return false;
        return boss.distanceTo(target) >= boss.getBbWidth() * 1.75F;
    }
    @Override public boolean canContinueToUse() { return timer < 70 && boss.isAlive(); }

    @Override public void start() {
        timer = 0;
        blast = boss.getRandom().nextBoolean();
        boss.setActionLocked(true);
        boss.getNavigation().stop();
        boss.playSound(ACSounds.LOD_ACTION_START.get(), 3.0F, 1.0F);
    }

    @Override public void tick() {
        timer++;
        boss.getNavigation().stop();
        LivingEntity target = boss.getTarget();
        if (target != null) boss.getLookControl().setLookAt(target, 30.0F, 30.0F);
        if (!(boss.level() instanceof ServerLevel level)) return;

        if (blast && timer == 21) {
            boss.playSound(ACSounds.LOD_SHOOT.get(), 5.0F, 0.6F);
            Vec3 muzzle = muzzle(-0.43633232F);
            LODInterceptBlast projectile = ACEntityTypes.LOD_INTERCEPT_BLAST.get().create(level);
            if (projectile != null) {
                projectile.moveTo(muzzle.x, muzzle.y, muzzle.z, boss.getYRot(), 0);
                projectile.size = 0.55F;
                projectile.setOwner(boss);
                projectile.shootFromRotation(boss, 0.0F, boss.getYRot(), 0.0F, 1.0F, 0.0F);
                level.addFreshEntity(projectile);
            }
        } else if (!blast && (timer == 21 || timer == 23 || timer == 25)) {
            boss.playSound(ACSounds.LOD_SHOOT.get(), 5.0F, 1.0F);
            Vec3 muzzle = muzzle(-0.43633232F);
            AAALevel.addParticle(level, new ParticleEmitterInfo(Archaion.prefix("echo_blast"))
                    .position(muzzle).scale(6.0F));
            for (int i = 0; i < 3; i++) {
                EchoStarProjectile star = ACEntityTypes.ECHO_STAR.get().create(level);
                if (star == null) continue;
                star.moveTo(muzzle.x, muzzle.y, muzzle.z, boss.getYRot(), boss.getXRot());
                star.setOwner(boss);
                float pitchOffset = -30.0F + (-1.0F + boss.getRandom().nextFloat() * 2.0F) * 30.0F;
                float yawOffset = (-1.0F + boss.getRandom().nextFloat() * 2.0F) * 65.0F;
                star.shootFromRotation(boss, boss.getXRot() + pitchOffset, boss.getYRot() + yawOffset, 0.0F, 0.9F, 0.0F);
                level.addFreshEntity(star);
            }
        }
    }

    private Vec3 muzzle(float yawOffset) {
        float yaw = boss.getYHeadRot() * ((float)Math.PI / 180.0F);
        Vec3 forward = new Vec3(-Mth.sin(yaw), 0, Mth.cos(yaw));
        return boss.position().add(forward.yRot(yawOffset).scale(6.0D)).add(0, 5.5D, 0);
    }

    @Override public void stop() {
        boss.setActionLocked(false);
        boss.shootingCooldown = switch (boss.getPhase()) {
            case 2 -> 0;
            case 1 -> 30 + boss.getRandom().nextInt(10);
            default -> 60 + boss.getRandom().nextInt(20);
        };
    }
}
