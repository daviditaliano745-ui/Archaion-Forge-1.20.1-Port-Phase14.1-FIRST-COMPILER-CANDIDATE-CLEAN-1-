package com.ratrod.archaion.block;

import com.ratrod.archaion.registry.ACBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import javax.annotation.Nullable;

public class HologramBlockEntity extends BlockEntity {
    private String text = "";
    public int clientTicks;

    public HologramBlockEntity(BlockPos pos, BlockState state) { super(ACBlockEntities.HOLOGRAM.get(), pos, state); }
    public String getText() { return text; }
    public void setText(String text) {
        this.text = text == null ? "" : text;
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }
    public int getTextColor() { return -6029339; }
    public static void clientTick(Level level, BlockPos pos, BlockState state, HologramBlockEntity entity) { entity.clientTicks++; }

    @Override public void load(CompoundTag tag) { super.load(tag); text = tag.getString("text"); }
    @Override protected void saveAdditional(CompoundTag tag) { super.saveAdditional(tag); tag.putString("text", text); }
    @Nullable @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public CompoundTag getUpdateTag() { return saveWithoutMetadata(); }
}
