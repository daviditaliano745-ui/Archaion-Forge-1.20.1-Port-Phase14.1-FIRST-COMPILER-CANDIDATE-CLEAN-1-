package com.ratrod.archaion.network.s2c;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** One-shot server -> client transfer of the Ancient Keep structure bounding box. */
public record AncientKeepAmbientPacket(AABB box) {
    public static void encode(AncientKeepAmbientPacket packet, FriendlyByteBuf buf) {
        AABB box = packet.box;
        buf.writeDouble(box.minX);
        buf.writeDouble(box.minY);
        buf.writeDouble(box.minZ);
        buf.writeDouble(box.maxX);
        buf.writeDouble(box.maxY);
        buf.writeDouble(box.maxZ);
    }

    public static AncientKeepAmbientPacket decode(FriendlyByteBuf buf) {
        return new AncientKeepAmbientPacket(new AABB(
                buf.readDouble(), buf.readDouble(), buf.readDouble(),
                buf.readDouble(), buf.readDouble(), buf.readDouble()));
    }

    public static void handle(AncientKeepAmbientPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientHandler.handle(packet)));
        context.setPacketHandled(true);
    }

    private static final class ClientHandler {
        private static void handle(AncientKeepAmbientPacket packet) {
            com.ratrod.archaion.client.misc.AncientKeepClientData.ANCIENT_KEEP_BOX = packet.box;
        }
    }
}
