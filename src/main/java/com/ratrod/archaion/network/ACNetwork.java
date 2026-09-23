package com.ratrod.archaion.network;

import com.ratrod.archaion.Archaion;
import com.ratrod.archaion.network.s2c.BossMusicPacket;
import com.ratrod.archaion.network.s2c.CameraShakePacket;
import com.ratrod.archaion.network.s2c.AncientKeepAmbientPacket;
import com.ratrod.archaion.network.s2c.SyncBossBarDataPacket;
import com.ratrod.archaion.network.s2c.RemoveBossBarDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

/** Forge 1.20.1 SimpleChannel replacement for Archaion's NeoForge payload registrar. */
public final class ACNetwork {
    private static final String PROTOCOL_VERSION = "1.0";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            Archaion.prefix("main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);
    private static int nextId;

    public static void register() {
        CHANNEL.registerMessage(nextId++, BossMusicPacket.class,
                BossMusicPacket::encode, BossMusicPacket::decode, BossMusicPacket::handle);
        CHANNEL.registerMessage(nextId++, CameraShakePacket.class,
                CameraShakePacket::encode, CameraShakePacket::decode, CameraShakePacket::handle);
        CHANNEL.registerMessage(nextId++, AncientKeepAmbientPacket.class,
                AncientKeepAmbientPacket::encode, AncientKeepAmbientPacket::decode, AncientKeepAmbientPacket::handle);
        CHANNEL.registerMessage(nextId++, SyncBossBarDataPacket.class,
                SyncBossBarDataPacket::encode, SyncBossBarDataPacket::decode, SyncBossBarDataPacket::handle);
        CHANNEL.registerMessage(nextId++, RemoveBossBarDataPacket.class,
                RemoveBossBarDataPacket::encode, RemoveBossBarDataPacket::decode, RemoveBossBarDataPacket::handle);
    }

    public static void sendToPlayer(ServerPlayer player, Object message) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static void sendToTrackingPlayers(Entity entity, Object message) {
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), message);
    }

    public static void sendToAll(Object message) {
        CHANNEL.send(PacketDistributor.ALL.noArg(), message);
    }

    private ACNetwork() { }
}
