package com.ratrod.archaion.block;

import com.ratrod.archaion.Archaion;
import com.ratrod.archaion.registry.ACBlockEntities;
import com.ratrod.archaion.registry.ACSounds;
import mod.chloeprime.aaaparticles.api.common.AAALevel;
import mod.chloeprime.aaaparticles.api.common.ParticleEmitterInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import javax.annotation.Nullable;

import java.util.HashSet;
import java.util.Set;

public class TeleporterBlockEntity extends BlockEntity {
    private static final Set<TeleporterBlockEntity> LOADED_TELEPORTERS = new HashSet<>();
    public int maxHeight;
    public int tickCount;
    private int cooldownTicks;

    public TeleporterBlockEntity(BlockPos pos, BlockState state) { super(ACBlockEntities.TELEPORTER.get(), pos, state); }
    public boolean isDisabled() { return cooldownTicks > 0; }
    public int getCooldownTicks() { return cooldownTicks; }

    public void setDisabled(boolean disabled) {
        int value = disabled ? 1200 : 0;
        if (cooldownTicks == value) return;
        cooldownTicks = value;
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TeleporterBlockEntity entity) {
        entity.tickCount++;
        if (entity.cooldownTicks > 0 && --entity.cooldownTicks == 0) {
            entity.setChanged();
            level.sendBlockUpdated(pos, state, state, 2);
        }
        // Original recalculated this every tick. Ten-tick caching preserves fast obstruction
        // response while avoiding a tall vertical scan for every loaded teleporter every server tick.
        if (entity.tickCount % 10 == 1 || entity.maxHeight <= 0) calculateBeamHeight(level, entity);
        if (!(level instanceof ServerLevel server) || entity.isDisabled()) return;
        AABB beam = new AABB(pos.getX() - 0.5D, pos.getY(), pos.getZ() - 0.5D,
                pos.getX() + 0.5D, pos.getY() + Math.max(1, entity.maxHeight), pos.getZ() + 0.5D);
        for (Player player : level.getEntitiesOfClass(Player.class, beam)) {
            if (entity.attemptTeleport(server, player)) break;
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, TeleporterBlockEntity entity) {
        entity.tickCount++;
        if (entity.cooldownTicks > 0) entity.cooldownTicks--;
        if (entity.tickCount % 10 == 1 || entity.maxHeight <= 0) calculateBeamHeight(level, entity);
    }

    private static void calculateBeamHeight(Level level, TeleporterBlockEntity entity) {
        int height = 0;
        BlockPos.MutableBlockPos cursor = entity.getBlockPos().above().mutable();
        int max = level.getMaxBuildHeight() - cursor.getY();
        while (height < max && !level.getBlockState(cursor).isFaceSturdy(level, cursor, Direction.DOWN)) {
            cursor.move(Direction.UP);
            height++;
        }
        entity.maxHeight = height + 1;
    }

    public boolean attemptTeleport(ServerLevel server, Entity target) {
        if (isDisabled()) return false;
        TeleporterColor color = getBlockState().getValue(TeleporterBlock.COLOR);
        TeleporterBlockEntity closest = null;
        double closestDistance = Double.MAX_VALUE;
        for (TeleporterBlockEntity candidate : LOADED_TELEPORTERS) {
            if (candidate == this || candidate.isDisabled() || candidate.level != this.level) continue;
            if (!candidate.getBlockState().hasProperty(TeleporterBlock.COLOR) || candidate.getBlockState().getValue(TeleporterBlock.COLOR) != color) continue;
            double distance = worldPosition.distSqr(candidate.worldPosition);
            if (distance > 0 && distance < closestDistance) { closestDistance = distance; closest = candidate; }
        }
        if (closest == null) return false;

        Vec3 source = Vec3.atCenterOf(worldPosition).add(0, 0.2D, 0);
        AAALevel.addParticle(server, true, new ParticleEmitterInfo(Archaion.prefix("teleporter_teleport")).position(source).scale(0.2F));
        server.playSound(null, worldPosition, ACSounds.TELEPORTER_WARPS.get(), SoundSource.BLOCKS, 2.0F, 1.0F);

        Vec3 destination = Vec3.atCenterOf(closest.worldPosition.above());
        target.teleportTo(destination.x, destination.y, destination.z);
        AAALevel.addParticle(server, true, new ParticleEmitterInfo(Archaion.prefix("teleporter_teleport"))
                .position(destination.add(0, -1.0D, 0)).scale(0.1F));
        server.playSound(null, BlockPos.containing(destination), ACSounds.TELEPORTER_WARPS.get(), SoundSource.BLOCKS, 2.0F, 1.0F);
        setDisabled(true);
        closest.setDisabled(true);
        return true;
    }

    @Override public void onLoad() { super.onLoad(); if (level != null && !level.isClientSide) LOADED_TELEPORTERS.add(this); }
    @Override public void setRemoved() { super.setRemoved(); LOADED_TELEPORTERS.remove(this); }
    @Override public void load(CompoundTag tag) { super.load(tag); cooldownTicks = tag.getInt("cooldown"); }
    @Override protected void saveAdditional(CompoundTag tag) { super.saveAdditional(tag); if (cooldownTicks > 0) tag.putInt("cooldown", cooldownTicks); }
    @Nullable @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public CompoundTag getUpdateTag() { return saveWithoutMetadata(); }
}
