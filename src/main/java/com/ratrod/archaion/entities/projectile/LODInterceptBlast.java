package com.ratrod.archaion.entities.projectile;

import com.ratrod.archaion.Archaion;
import com.ratrod.archaion.entities.LastOfDeepslate;
import com.ratrod.archaion.registry.ACSounds;
import mod.chloeprime.aaaparticles.api.common.AAALevel;
import mod.chloeprime.aaaparticles.api.common.ParticleEmitterInfo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/** Repeating moving blast field used by the boss's blast/intercept shots. */
public class LODInterceptBlast extends ThrowableProjectile {
    public float size = 1.0F;

    public LODInterceptBlast(EntityType<? extends LODInterceptBlast> type, Level level) {
        super(type, level);
        setNoGravity(true);
    }

    @Override protected void defineSynchedData() { }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && !isRemoved() && tickCount % 2 == 0) blast();
        if (tickCount >= 100) discard();
    }

    @Override protected void onHitBlock(BlockHitResult hit) { super.onHitBlock(hit); if (!level().isClientSide) discard(); }
    @Override protected void onHitEntity(EntityHitResult hit) { super.onHitEntity(hit); }

    public void blast() {
        if (!(level() instanceof ServerLevel server)) return;
        playSound(ACSounds.ECHO_STAR_BLAST.get(), 5.0F,
                (0.5F + random.nextFloat() * 0.2F) * (1.8F - size));
        AAALevel.addParticle(server, new ParticleEmitterInfo(Archaion.prefix("echo_blast_intercept"))
                .position(position()).rotation(0, random.nextFloat() * 90.0F, 0).scale(3.0F * size));

        Entity owner = getOwner() != null ? getOwner() : this;
        AABB area = AABB.ofSize(position(), 14.0D * size, 14.0D * size, 14.0D * size);
        for (LivingEntity living : server.getEntitiesOfClass(LivingEntity.class, area,
                target -> target != owner && target.isAlive() && canTarget(target))) {
            living.hurt(server.damageSources().explosion(owner, owner), 55.0F * size);
            Vec3 push = living.position().subtract(position());
            if (push.lengthSqr() > 1.0E-6D) {
                push = push.normalize().scale(3.0D).add(0, 0.35D, 0);
                living.setDeltaMovement(living.getDeltaMovement().add(push));
                living.hurtMarked = true;
            }
        }
    }

    private boolean canTarget(LivingEntity living) {
        return !(getOwner() instanceof LastOfDeepslate boss) || boss.canAttack(living);
    }
}
