package com.ratrod.archaion;

import com.ratrod.archaion.client.audio.LODSoundInstance;
import net.minecraft.client.Minecraft;
import com.ratrod.archaion.client.misc.AncientKeepClientData;
import com.ratrod.archaion.client.misc.BossbarRenderers;
import com.ratrod.archaion.client.misc.ClientBossBarData;
import com.ratrod.archaion.client.misc.LastOfDeepslateTooltipRenderer;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Client Forge-bus hooks for the restored Ancient Keep atmosphere and Last of Deepslate HUD. */
@Mod.EventBusSubscriber(modid = Archaion.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ACClientEvents {
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !event.player.level().isClientSide) return;
        if (event.player != Minecraft.getInstance().player) return;
        AncientKeepClientData.tick(event.player);
        LODSoundInstance.tryToRepair();
    }

    @SubscribeEvent
    public static void renderBossbar(CustomizeGuiOverlayEvent.BossEventProgress event) {
        int bossIdx = ClientBossBarData.getBossIdx(event.getBossEvent().getId());
        if (bossIdx == 0) {
            event.setCanceled(true);
            BossbarRenderers.renderLastOfDeepslate(event);
        }
    }

    @SubscribeEvent
    public static void renderSleepingBossTooltip(RenderGuiEvent.Post event) {
        LastOfDeepslateTooltipRenderer.render(event.getGuiGraphics(), event.getPartialTick());
    }

    @SubscribeEvent
    public static void renderAncientKeepFog(ViewportEvent.RenderFog event) {
        float mix = AncientKeepClientData.keepFogFactor();
        if (mix <= 0.0F) return;
        float near = event.getNearPlaneDistance();
        float far = event.getFarPlaneDistance();
        event.setNearPlaneDistance(Mth.lerp(mix, near, 16.0F));
        event.setFarPlaneDistance(Mth.lerp(mix, far, 128.0F));
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void renderAncientKeepFogColor(ViewportEvent.ComputeFogColor event) {
        float mix = AncientKeepClientData.keepFogFactor();
        if (mix <= 0.0F) return;
        event.setRed(Mth.lerp(mix, event.getRed(), 0.39F));
        event.setGreen(Mth.lerp(mix, event.getGreen(), 0.898F));
        event.setBlue(Mth.lerp(mix, event.getBlue(), 1.0F));
    }

    private ACClientEvents() { }
}
