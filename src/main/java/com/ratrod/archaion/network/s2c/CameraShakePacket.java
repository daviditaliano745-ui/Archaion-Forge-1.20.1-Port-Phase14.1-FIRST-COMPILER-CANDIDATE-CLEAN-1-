package com.ratrod.archaion.network.s2c;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record CameraShakePacket(float intensity, int duration, float frequency) {
    public static void encode(CameraShakePacket packet, FriendlyByteBuf buf) {
        buf.writeFloat(packet.intensity);
        buf.writeVarInt(packet.duration);
        buf.writeFloat(packet.frequency);
    }

    public static CameraShakePacket decode(FriendlyByteBuf buf) {
        return new CameraShakePacket(buf.readFloat(), buf.readVarInt(), buf.readFloat());
    }

    public static void handle(CameraShakePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientHandler.handle(packet)));
        context.setPacketHandled(true);
    }

    /** Kept behind a nested class so a dedicated server never resolves the client handler. */
    private static final class ClientHandler {
        private static void handle(CameraShakePacket packet) {
            com.ratrod.archaion.client.camera.ScreenShakeHandler.shakeLocal(
                    packet.intensity, packet.duration, packet.frequency);
        }
    }
}
