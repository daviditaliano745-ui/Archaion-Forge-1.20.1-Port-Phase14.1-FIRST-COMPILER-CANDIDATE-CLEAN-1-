package com.ratrod.archaion.entities.projectile;

import com.ratrod.archaion.Archaion;
import com.ratrod.archaion.registry.ACEntityTypes;
import mod.chloeprime.aaaparticles.api.common.AAALevel;
import mod.chloeprime.aaaparticles.api.common.ParticleEmitterInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/** Functional 1.20.1 replacement for the 1.21 thrown Echo Mace projectile. */
public class ThrownEchoMace extends ThrowableProjectile implements ItemSupplier {
    private static final EntityDataAccessor<ItemStack> DATA_MACE_STACK =
            SynchedEntityData.defineId(ThrownEchoMace.class, EntityDataSerializers.ITEM_STACK);
    public int bounces;

    public ThrownEchoMace(EntityType<? extends ThrownEchoMace> type, Level level) { super(type, level); }
    public ThrownEchoMace(Level level, LivingEntity owner, ItemStack stack) {
        this(ACEntityTypes.THROWN_ECHO_MACE.get(), level);
        setOwner(owner);
        setPos(owner.getX(), owner.getEyeY() - 0.1D, owner.getZ());
        ItemStack copy = stack.copy();
        copy.setCount(1);
        entityData.set(DATA_MACE_STACK, copy);
    }

    @Override protected void defineSynchedData() { entityData.define(DATA_MACE_STACK, ItemStack.EMPTY); }
    public ItemStack getThrownStack() { return entityData.get(DATA_MACE_STACK); }
    @Override public ItemStack getItem() { return getThrownStack(); }
    @Override protected float getGravity() { return 0.03F; }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide && tickCount % 2 == 0) {
            level().addParticle(ParticleTypes.SCULK_SOUL, getX(), getY(), getZ(), 0, 0, 0);
        }
        // Original projectile is no-save and normally dies through its bounce counter; this is only a runaway guard.
        if (tickCount > 300) discard();
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        super.onHitEntity(hit);
        if (!(level() instanceof ServerLevel server)) return;
        Entity target = hit.getEntity();
        Entity owner = getOwner();
        float damage = 8.0F + tickCount * 0.5F;
        boolean hurt = target.hurt(damageSources().thrown(this, owner == null ? this : owner), damage);
        if (hurt) {
            if (target instanceof LivingEntity living) {
                Vec3 motion = getDeltaMovement();
                living.push(motion.x * 0.4D, 0.3D, motion.z * 0.4D);
            }
            server.playSound(null, blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 2.0F, 1.2F);
            spawnSmashEffect(server, target.getOnPos(), target);
            setDeltaMovement(0, 1, 0);
        } else {
            server.playSound(null, blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 2.0F, 1.0F);
            spawnSmashEffect(server, target.getOnPos(), null);
            discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult hit) {
        super.onHitBlock(hit);
        if (!(level() instanceof ServerLevel server)) return;
        spawnSmashEffect(server, hit.getBlockPos(), null);
        Vec3 motion = getDeltaMovement();
        Direction direction = hit.getDirection();
        Vec3 normal = Vec3.atLowerCornerOf(direction.getNormal());
        double dot = motion.dot(normal);
        setDeltaMovement(motion.subtract(normal.scale(1.75D * dot)));
        setPos(position().add(normal.scale(0.05D)));
        server.playSound(null, blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 2.0F, 1.0F);
    }

    private void spawnSmashEffect(ServerLevel server, BlockPos pos, Entity struck) {
        AAALevel.addParticle(server, new ParticleEmitterInfo(Archaion.prefix("echo_blast"))
                .position(Vec3.atCenterOf(pos)).scale(1.25F));
        bounces++;
        if (bounces > 3) discard();
    }
}
