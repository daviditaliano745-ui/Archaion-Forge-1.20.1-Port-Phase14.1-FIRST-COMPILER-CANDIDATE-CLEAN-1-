package com.ratrod.archaion.entities.effect;

import com.ratrod.archaion.Archaion;
import com.ratrod.archaion.entities.LastOfDeepslate;
import com.ratrod.archaion.registry.ACEntityTypes;
import com.ratrod.archaion.registry.ACSounds;
import mod.chloeprime.aaaparticles.api.common.AAALevel;
import mod.chloeprime.aaaparticles.api.common.ParticleEmitterInfo;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

/** Expanding ground-wave arm produced by the phase-3 body slam. */
public class LODSlamEffect extends Entity {
    private Vec3 slamDirection = new Vec3(1, 0, 0);
    private int generation = 1;
    private LastOfDeepslate source;
    private boolean damaged;

    public LODSlamEffect(EntityType<? extends LODSlamEffect> type, Level level) { super(type, level); }

    public static void summonRing(ServerLevel level, LastOfDeepslate source) {
        double baseAngle = source.getRandom().nextDouble() * Math.PI * 2.0D;
        for (int i = 0; i < 12; i++) {
            double angle = baseAngle + Math.PI * 2.0D * i / 12.0D;
            Vec3 dir = new Vec3(Math.cos(angle), 0, Math.sin(angle));
            LODSlamEffect effect = ACEntityTypes.LOD_SLAM.get().create(level);
            if (effect == null) continue;
            effect.source = source;
            effect.slamDirection = dir;
            effect.generation = 1;
            Vec3 at = placeOnGround(level, source.position().add(dir.scale(4.0D)));
            effect.setPos(at.x, at.y, at.z);
            level.addFreshEntity(effect);
        }
    }

    @Override protected void defineSynchedData() { }
    @Override protected void readAdditionalSaveData(CompoundTag tag) { }
    @Override protected void addAdditionalSaveData(CompoundTag tag) { }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide && tickCount == 1) {
            AAALevel.addParticle(level(), new ParticleEmitterInfo(Archaion.prefix("lod_smash_initial"))
                    .position(position()).scale(2.0F));
        }
        if (!(level() instanceof ServerLevel server)) return;
        if (tickCount == 2 && generation < 10) spawnChild(server);
        if (tickCount >= 20) {
            damageArea(server);
            discard();
        }
    }

    private void spawnChild(ServerLevel level) {
        LODSlamEffect child = ACEntityTypes.LOD_SLAM.get().create(level);
        if (child == null) return;
        child.source = source;
        child.slamDirection = slamDirection;
        child.generation = generation + 1;
        Vec3 at = placeOnGround(level, position().add(slamDirection.scale(4.0D)));
        child.setPos(at.x, at.y, at.z);
        level.addFreshEntity(child);
    }

    private void damageArea(ServerLevel level) {
        if (damaged) return;
        damaged = true;
        playSound(ACSounds.LOD_SMASH.get(), 1.2F, 1.8F);
        AABB box = AABB.ofSize(position(), 3.0D, 12.0D, 3.0D);
        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, box,
                target -> target != source && target.isAlive() && (source == null || source.canAttack(target)))) {
            living.hurt(level.damageSources().explosion(this, source), 30.0F);
        }
    }

    private static Vec3 placeOnGround(ServerLevel level, Vec3 raw) {
        int x = net.minecraft.util.Mth.floor(raw.x);
        int z = net.minecraft.util.Mth.floor(raw.z);
        int y = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        return new Vec3(raw.x, y + 0.05D, raw.z);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
