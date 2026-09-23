package com.ratrod.archaion.entities.projectile;

import com.ratrod.archaion.Archaion;
import com.ratrod.archaion.entities.Grimoray;
import com.ratrod.archaion.entities.GrimorayType;
import com.ratrod.archaion.registry.ACEntityTypes;
import mod.chloeprime.aaaparticles.api.common.AAALevel;
import mod.chloeprime.aaaparticles.api.common.ParticleEmitterInfo;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/** Forge 1.20.1 port of the Grimoray spell projectile. */
public class GrimoraySpellProjectile extends ThrowableProjectile {
    private static final EntityDataAccessor<Integer> GRIMORAY_TYPE =
            SynchedEntityData.defineId(GrimoraySpellProjectile.class, EntityDataSerializers.INT);

    public GrimoraySpellProjectile(EntityType<? extends GrimoraySpellProjectile> type, Level level) {
        super(type, level);
    }

    public GrimoraySpellProjectile(Level level, LivingEntity owner) {
        this(ACEntityTypes.GRIMORAY_SPELL.get(), level);
        setOwner(owner);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(GRIMORAY_TYPE, GrimorayType.POISON_CLOUD.ordinal());
    }

    public GrimorayType getGrimorayType() {
        int id = entityData.get(GRIMORAY_TYPE);
        GrimorayType[] values = GrimorayType.values();
        return id >= 0 && id < values.length ? values[id] : GrimorayType.POISON_CLOUD;
    }

    public void setGrimorayType(GrimorayType type) {
        entityData.set(GRIMORAY_TYPE, type.ordinal());
    }

    /** Exposed for the original ballistic launch calculation. */
    public double getProjectileGravity() {
        return 0.05D;
    }

    @Override
    protected float getGravity() {
        return 0.05F;
    }

    @Override
    protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (level() instanceof ServerLevel serverLevel) applyEffect(serverLevel, hit.getLocation());
        discard();
    }

    private void applyEffect(ServerLevel level, Vec3 hitPos) {
        switch (getGrimorayType()) {
            case POISON_CLOUD -> {
                AreaEffectCloud cloud = new AreaEffectCloud(level, hitPos.x, hitPos.y, hitPos.z);
                if (getOwner() instanceof LivingEntity owner) cloud.setOwner(owner);
                cloud.setRadius(3.0F);
                cloud.setDuration(100);
                cloud.setWaitTime(10);
                cloud.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 0));
                level.addFreshEntity(cloud);
                playSound(SoundEvents.SQUID_DEATH, 1.5F, 1.0F);
            }
            case HARMING -> {
                Entity owner = getOwner();
                AABB area = AABB.ofSize(hitPos, 4.0D, 4.0D, 4.0D);
                List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area,
                        entity -> entity != owner && !(entity instanceof Grimoray) && entity.isAffectedByPotions());
                for (LivingEntity living : targets) {
                    MobEffects.HARM.applyInstantenousEffect(this, owner, living, 0, 1.0D);
                }
                playSound(SoundEvents.SQUID_DEATH, 1.5F, 0.8F);
            }
            case HEALING -> {
                // Healing Grimorays do not fire this projectile in the original implementation.
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            String effect = switch (getGrimorayType()) {
                case POISON_CLOUD -> "grimoray_spell_poison";
                case HARMING -> "grimoray_spell_harming";
                case HEALING -> null;
            };
            if (effect != null) {
                ParticleEmitterInfo particle = new ParticleEmitterInfo(Archaion.prefix(effect));
                AAALevel.addParticle(level(), particle.position(position()).scale(0.15F));
            }
        }
        if (tickCount > 50) discard();
    }
}
