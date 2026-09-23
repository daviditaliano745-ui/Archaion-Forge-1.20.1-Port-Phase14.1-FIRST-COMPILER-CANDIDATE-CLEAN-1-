package com.ratrod.archaion.entities.ai.goals;

import com.ratrod.archaion.Archaion;
import com.ratrod.archaion.entities.Grimoray;
import com.ratrod.archaion.entities.GrimorayType;
import com.ratrod.archaion.entities.projectile.GrimoraySpellProjectile;
import com.ratrod.archaion.registry.ACEntityTypes;
import mod.chloeprime.aaaparticles.api.common.AAALevel;
import mod.chloeprime.aaaparticles.api.common.ParticleEmitterInfo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

/** Native 1.20.1 replacement for the original GrimorayShootAction. */
public class GrimorayShootGoal extends Goal {
    private final Grimoray grimoray;
    private int timer;

    public GrimorayShootGoal(Grimoray grimoray) {
        this.grimoray = grimoray;
        setFlags(EnumSet.of(Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = grimoray.getTarget();
        if (target == null || !target.isAlive() || grimoray.getSpellCooldown() > 0) return false;
        double distance = grimoray.distanceTo(target);
        return distance >= 3.0D && distance <= 16.0D && grimoray.getSensing().hasLineOfSight(target);
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = grimoray.getTarget();
        return target != null && target.isAlive() && timer <= 30;
    }

    @Override
    public void start() {
        timer = 0;
    }

    @Override
    public void stop() {
        grimoray.setSpellCooldown(grimoray.getSpellCDDuration());
    }

    @Override
    public void tick() {
        LivingEntity target = grimoray.getTarget();
        if (target == null) return;
        timer++;
        grimoray.getLookControl().setLookAt(target, 30.0F, 30.0F);
        if (timer != 20 || !(grimoray.level() instanceof ServerLevel serverLevel)) return;

        if (grimoray.getGrimorayType() == GrimorayType.HEALING) {
            castHealing(serverLevel);
        } else {
            castProjectile(serverLevel, target);
        }
    }

    private void castHealing(ServerLevel level) {
        grimoray.playSound(SoundEvents.PLAYER_LEVELUP, 1.5F, 1.5F);
        List<LivingEntity> allies = level.getEntitiesOfClass(LivingEntity.class,
                grimoray.getBoundingBox().inflate(16.0D), entity -> entity instanceof Enemy && entity.isAlive());

        for (LivingEntity entity : allies) {
            boolean self = entity == grimoray;
            entity.heal(self ? 4.0F : 3.0F);
            ParticleEmitterInfo particle = new ParticleEmitterInfo(Archaion.prefix(
                    self ? "grimoray_spell_healing_core" : "grimoray_spell_healing"));
            AAALevel.addParticle(level, particle.bindOnEntity(entity).scale(self ? 1.5F : 1.0F));
        }
    }

    private void castProjectile(ServerLevel level, LivingEntity target) {
        Vec3 spawn = grimoray.position().add(0.0D, grimoray.getBbHeight() * 0.6D, 0.0D);
        grimoray.playSound(SoundEvents.SQUID_SQUIRT, 1.5F, 0.8F);

        GrimoraySpellProjectile projectile = ACEntityTypes.GRIMORAY_SPELL.get().create(level);
        if (projectile == null) return;
        projectile.moveTo(spawn.x, spawn.y, spawn.z, grimoray.getYRot(), grimoray.getXRot());
        projectile.setOwner(grimoray);
        projectile.setGrimorayType(grimoray.getGrimorayType());

        float dx = (float) (target.getX() - spawn.x);
        float dz = (float) (target.getZ() - spawn.z);
        float vertical = (float) (spawn.y - target.getY());
        float horizontal = (float) Math.sqrt(dx * dx + dz * dz);

        float time = (float) Math.sqrt((2.0F * vertical) / projectile.getProjectileGravity());
        float speed = Float.isFinite(time) ? Math.min(horizontal / time, 0.9F) : 0.5F;
        Vec3 horizontalDirection = horizontal < 1.0E-4F
                ? Vec3.ZERO
                : new Vec3(dx / horizontal, 0.0D, dz / horizontal);

        projectile.setDeltaMovement(horizontalDirection.scale(speed).add(0.0D, 0.1D, 0.0D));
        level.addFreshEntity(projectile);
    }
}
