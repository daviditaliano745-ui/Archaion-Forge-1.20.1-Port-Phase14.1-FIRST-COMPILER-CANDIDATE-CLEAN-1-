package com.ratrod.archaion.entities.projectile;

import com.ratrod.archaion.registry.ACEntityTypes;
import com.ratrod.archaion.registry.ACItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/** 1.20.1-compatible implementation of Archaion's Impact Pearl projectile. */
public class ThrownImpactPearl extends ThrownEnderpearl {
    public ThrownImpactPearl(EntityType<? extends ThrownEnderpearl> type, Level level) {
        super(type, level);
    }

    public ThrownImpactPearl(Level level, LivingEntity owner, ItemStack stack) {
        this(ACEntityTypes.THROWN_IMPACT_PEARL.get(), level);
        moveTo(owner.getX(), owner.getEyeY() - 0.1D, owner.getZ(), owner.getYRot(), owner.getXRot());
        setOwner(owner);
        setItem(stack);
    }

    @Override
    protected Item getDefaultItem() {
        return ACItems.IMPACT_PEARL.get();
    }

    public void impact(ServerLevel level) {
        BlockPos impactPos = getOnPos();
        BlockState state = level.getBlockState(impactPos);
        if (state.isAir()) impactPos = impactPos.below();

        // 1.21 used the vanilla mace heavy-smash sound/event. Those do not exist in 1.20.1,
        // so preserve gameplay and use compatible vanilla feedback until a custom Archaion sound is wired.
        playSound(SoundEvents.GENERIC_EXPLODE, 1.0F, 1.25F);
        Vec3 center = Vec3.atCenterOf(impactPos).add(0.0D, 0.5D, 0.0D);
        level.sendParticles(ParticleTypes.POOF, center.x, center.y, center.z, 24, 1.3D, 0.35D, 1.3D, 0.08D);

        int radius = 4;
        AABB box = AABB.ofSize(center, radius * 2.0D, 6.0D, radius * 2.0D);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, box,
                living -> living != getOwner() && living.isAlive());

        for (LivingEntity living : targets) {
            float damage = getOwner() == null ? 5.0F : 5.0F + distanceTo(getOwner()) * 0.2F;
            if (living.hurt(level.damageSources().thrown(this, getOwner()), damage)) {
                Vec3 knockback = living.position().subtract(center).normalize().scale(1.8D);
                living.setDeltaMovement(living.getDeltaMovement().add(knockback));
                living.hurtMarked = true;
            }
        }
        discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (level() instanceof ServerLevel serverLevel) impact(serverLevel);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (level() instanceof ServerLevel serverLevel) impact(serverLevel);
    }
}
