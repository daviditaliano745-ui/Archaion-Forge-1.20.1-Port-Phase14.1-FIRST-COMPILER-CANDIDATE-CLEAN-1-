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
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import java.util.UUID;

/** Server-authoritative falling terrain chunk spawned by LOD's ground smash. */
public class LODFallingBlock extends Entity {
    private static final EntityDataAccessor<Integer> BLOCK_STATE =
            SynchedEntityData.defineId(LODFallingBlock.class, EntityDataSerializers.INT);
    private UUID ownerId;

    public LODFallingBlock(EntityType<? extends LODFallingBlock> type, Level level) { super(type, level); }

    public static void spawn(ServerLevel level, BlockState state, Vec3 pos, LastOfDeepslate owner) {
        LODFallingBlock entity = ACEntityTypes.LOD_FALLING_BLOCK.get().create(level);
        if (entity == null) return;
        entity.setBlockState(state);
        entity.ownerId = owner.getUUID();
        entity.setPos(pos.x, pos.y + 0.5D, pos.z);
        entity.setDeltaMovement((owner.getRandom().nextDouble() - 0.5D) * 0.15D,
                0.7D + owner.getRandom().nextDouble() * 0.6D,
                (owner.getRandom().nextDouble() - 0.5D) * 0.15D);
        level.addFreshEntity(entity);
    }

    @Override protected void defineSynchedData() { entityData.define(BLOCK_STATE, 0); }
    public BlockState getBlockState() { return Block.stateById(entityData.get(BLOCK_STATE)); }
    public void setBlockState(BlockState state) { entityData.set(BLOCK_STATE, Block.getId(state)); }

    @Override
    public void tick() {
        super.tick();
        if (!isNoGravity()) setDeltaMovement(getDeltaMovement().add(0, -0.04D, 0));
        move(MoverType.SELF, getDeltaMovement());
        setDeltaMovement(getDeltaMovement().scale(0.98D));
        if (!level().isClientSide && (onGround() || tickCount > 600)) impact();
    }

    private void impact() {
        if (!(level() instanceof ServerLevel level)) { discard(); return; }
        Entity owner = ownerId == null ? null : level.getEntity(ownerId);
        playSound(ACSounds.LOD_BLOCK_FALL.get(), 1.2F, 1.0F);
        AAALevel.addParticle(level, new ParticleEmitterInfo(Archaion.prefix("lod_falling_block"))
                .position(position()).scale(1.0F));
        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(2.0D, 1.0D, 2.0D),
                target -> target != owner && target.isAlive())) {
            if (owner instanceof LastOfDeepslate boss && !boss.canAttack(living)) continue;
            living.hurt(level.damageSources().explosion(this, owner), 25.0F);
        }
        discard();
    }

    @Override protected void readAdditionalSaveData(CompoundTag tag) { }
    @Override protected void addAdditionalSaveData(CompoundTag tag) { }
    @Override public Packet<ClientGamePacketListener> getAddEntityPacket() { return NetworkHooks.getEntitySpawningPacket(this); }
}
