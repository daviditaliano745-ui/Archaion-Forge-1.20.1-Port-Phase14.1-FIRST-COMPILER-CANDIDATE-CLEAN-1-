package com.ratrod.archaion.network.s2c;

import com.ratrod.archaion.misc.LODTheme;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record BossMusicPacket(LODTheme theme) {
    public static void encode(BossMusicPacket packet, FriendlyByteBuf buf) {
        buf.writeEnum(packet.theme);
    }

    public static BossMusicPacket decode(FriendlyByteBuf buf) {
        return new BossMusicPacket(buf.readEnum(LODTheme.class));
    }

    public static void handle(BossMusicPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientHandler.handle(packet)));
        context.setPacketHandled(true);
    }

    private static final class ClientHandler {
        private static void handle(BossMusicPacket packet) {
            if (packet.theme == LODTheme.STOP) {
                com.ratrod.archaion.client.audio.LODSoundInstance.fadeOut();
            } else {
                com.ratrod.archaion.client.audio.LODSoundInstance.startPhase(packet.theme);
            }
        }
    }
}
