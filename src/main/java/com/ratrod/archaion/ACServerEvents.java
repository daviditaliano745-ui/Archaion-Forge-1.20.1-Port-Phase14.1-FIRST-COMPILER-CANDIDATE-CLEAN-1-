package com.ratrod.archaion;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Server-side Forge event hooks retained from the original Ancient Keep ambience system. */
@Mod.EventBusSubscriber(modid = Archaion.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ACServerEvents {
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        if (player.tickCount % 10 != 0) return;
        AncientKeepServerData.tickPlayer(player);
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        AncientKeepServerData.onPlayerLoggedOut(event.getEntity());
    }

    private ACServerEvents() { }
}
