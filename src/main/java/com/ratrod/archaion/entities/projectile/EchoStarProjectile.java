package com.ratrod.archaion.entities.projectile;

import com.ratrod.archaion.Archaion;
import com.ratrod.archaion.entities.LastOfDeepslate;
import com.ratrod.archaion.entities.SleepingState;
import com.ratrod.archaion.registry.ACEntityTypes;
import com.ratrod.archaion.registry.ACSounds;
import mod.chloeprime.aaaparticles.api.common.AAALevel;
import mod.chloeprime.aaaparticles.api.common.ParticleEmitterInfo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

/** Forge 1.20.1 port of the Echo Star projectile used by Last of Deepslate. */
public class EchoStarProjectile extends ThrowableProjectile {
    private float baseDamage = 20.0F;
    private float powerBonus;

    public EchoStarProjectile(EntityType<? extends EchoStarProjectile> type, Level level) { super(type, level); }
    public EchoStarProjectile(Level level, LivingEntity owner) {
        this(ACEntityTypes.ECHO_STAR.get(), level);
        moveTo(owner.getX(), owner.getEyeY() - 0.1D, owner.getZ(), owner.getYRot(), owner.getXRot());
        setOwner(owner);
    }

    @Override protected void defineSynchedData() { }
    @Override protected float getGravity() { return 0.02F; }

    @Override
    public void tick() {
        if (level().isClientSide) {
            if (tickCount == 0) {
                AAALevel.addParticle(level(), new ParticleEmitterInfo(Archaion.prefix("echo_star"))
                        .bindOnEntity(this).position(0, 0.5D, 0).scale(0.6F));
            }
            if (tickCount % 2 == 0) {
                AAALevel.addParticle(level(), new ParticleEmitterInfo(Archaion.prefix("lod_boom"))
                        .position(position().add(0, 0.5D, 0)).scale(2.0F));
            }
            if (getOwner() == null) { discard(); return; }
        }
        super.tick();
        if (tickCount >= 100) discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult hit) {
        super.onHitBlock(hit);
        if (!level().isClientSide) damageArea();
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        super.onHitEntity(hit);
        if (!level().isClientSide) {
            if (hit.getEntity() instanceof LastOfDeepslate boss && boss.getSleepingState() == SleepingState.SLEEPING) {
                boss.feedEchoCharge();
                discard();
                return;
            }
            damageArea();
        }
    }

    public void setPowerBonus(float bonus) { powerBonus = bonus; }
    public void setBaseDamage(float damage) { baseDamage = damage; }

    public void damageArea() {
        if (!(level() instanceof ServerLevel server)) return;
        float damage = baseDamage + powerBonus;
        Entity owner = getOwner();
        for (LivingEntity living : server.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(3.0D))) {
            if (living == owner || !canHurt(living)) continue;
            living.hurt(server.damageSources().explosion(this, owner), damage);
        }
        playSound(ACSounds.ECHO_STAR_BLAST.get(), 4.0F, 1.0F);
        AAALevel.addParticle(server, new ParticleEmitterInfo(Archaion.prefix("lod_boom_group"))
                .position(position().add(0, 0.5D, 0)).scale(1.5F));
        discard();
    }

    private boolean canHurt(LivingEntity living) {
        return !(getOwner() instanceof LastOfDeepslate boss) || boss.canAttack(living);
    }
}
