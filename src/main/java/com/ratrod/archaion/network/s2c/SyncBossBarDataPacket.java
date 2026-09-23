package com.ratrod.archaion.network.s2c;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/** Event-driven custom boss-HUD data sync. */
public record SyncBossBarDataPacket(UUID bossBarId, int bossIdx, Map<String, Integer> values) {
    private static final int MAX_VALUES = 16;

    public static void encode(SyncBossBarDataPacket packet, FriendlyByteBuf buf) {
        buf.writeUUID(packet.bossBarId);
        buf.writeVarInt(packet.bossIdx);
        int size = Math.min(packet.values.size(), MAX_VALUES);
        buf.writeVarInt(size);
        int written = 0;
        for (Map.Entry<String, Integer> entry : packet.values.entrySet()) {
            if (written++ >= size) break;
            buf.writeUtf(entry.getKey(), 64);
            buf.writeInt(entry.getValue());
        }
    }

    public static SyncBossBarDataPacket decode(FriendlyByteBuf buf) {
        UUID id = buf.readUUID();
        int bossIdx = buf.readVarInt();
        int size = buf.readVarInt();
        if (size < 0 || size > MAX_VALUES) {
            throw new IllegalArgumentException("Invalid boss-bar value count: " + size);
        }
        Map<String, Integer> values = new HashMap<>();
        for (int i = 0; i < size; i++) {
            values.put(buf.readUtf(64), buf.readInt());
        }
        return new SyncBossBarDataPacket(id, bossIdx, Map.copyOf(values));
    }

    public static void handle(SyncBossBarDataPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientHandler.handle(packet)));
        context.setPacketHandled(true);
    }

    private static final class ClientHandler {
        private static void handle(SyncBossBarDataPacket packet) {
            com.ratrod.archaion.client.misc.ClientBossBarData.setBossData(
                    packet.bossBarId, packet.bossIdx, packet.values);
        }
    }
}
