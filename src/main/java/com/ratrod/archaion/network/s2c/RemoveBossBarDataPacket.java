package com.ratrod.archaion.network.s2c;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/** Drops client-side custom HUD state when a vanilla boss event stops tracking. */
public record RemoveBossBarDataPacket(UUID bossBarId) {
    public static void encode(RemoveBossBarDataPacket packet, FriendlyByteBuf buf) {
        buf.writeUUID(packet.bossBarId);
    }

    public static RemoveBossBarDataPacket decode(FriendlyByteBuf buf) {
        return new RemoveBossBarDataPacket(buf.readUUID());
    }

    public static void handle(RemoveBossBarDataPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientHandler.handle(packet)));
        context.setPacketHandled(true);
    }

    private static final class ClientHandler {
        private static void handle(RemoveBossBarDataPacket packet) {
            com.ratrod.archaion.client.misc.ClientBossBarData.removeBossBar(packet.bossBarId);
        }
    }
}
