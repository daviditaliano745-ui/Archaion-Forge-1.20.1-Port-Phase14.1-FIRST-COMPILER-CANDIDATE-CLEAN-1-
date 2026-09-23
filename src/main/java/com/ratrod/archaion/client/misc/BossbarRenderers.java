package com.ratrod.archaion.client.misc;

import com.ratrod.archaion.Archaion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;

import java.util.Map;

/** Custom Last of Deepslate boss HUD backported from the original client renderer. */
public final class BossbarRenderers {
    private static final ResourceLocation BARS_LOCATION = Archaion.prefix("textures/gui/hud_misc.png");

    public static void renderLastOfDeepslate(CustomizeGuiOverlayEvent.BossEventProgress event) {
        GuiGraphics graphics = event.getGuiGraphics();
        Minecraft minecraft = Minecraft.getInstance();
        int x = event.getX();
        int y = event.getY();
        float progress = event.getBossEvent().getProgress();
        Map<String, Integer> values = ClientBossBarData.getValues(event.getBossEvent().getId());
        int archaicPhase = values.getOrDefault("archaicPhase", 0);

        graphics.blit(BARS_LOCATION, x, y, 0.0F, 0.0F, 192, 32, 256, 256);

        float time = (minecraft.player != null ? minecraft.player.tickCount : 0) + event.getPartialTick();
        int sliceWidth = 4;
        int waveSpeed = archaicPhase == 1 ? 1 : 3;

        for (int sliceX = 0; sliceX < 192; sliceX += sliceWidth) {
            int width = Math.min(sliceWidth, 192 - sliceX);
            int yOffset = archaicPhase >= 1
                    ? (int) (Mth.sin(time * 0.075F * waveSpeed + sliceX * 0.05F) * 4.0F)
                    : 0;
            graphics.blit(BARS_LOCATION, x + sliceX, y + yOffset,
                    (float) sliceX, 64.0F, width, 32, 256, 256);
        }

        int filledWidth = (int) (192.0F * progress);
        for (int sliceX = 0; sliceX < filledWidth; sliceX += sliceWidth) {
            int width = Math.min(sliceWidth, filledWidth - sliceX);
            int yOffset = archaicPhase >= 1
                    ? (int) (Mth.sin(time * 0.075F * waveSpeed + sliceX * 0.05F) * 4.0F)
                    : 0;
            graphics.blit(BARS_LOCATION, x + sliceX, y + yOffset + 1,
                    (float) sliceX, 32.0F, width, 32, 256, 256);
        }

        int archaicRaidAlive = values.getOrDefault("archaicRaidAlive", 0);
        int archaicRaidTotal = values.getOrDefault("archaicRaidTotal", 0);
        int raidBarXOffset = 42;
        if (archaicRaidTotal > 0 && archaicRaidAlive > 0) {
            graphics.blit(BARS_LOCATION, x + raidBarXOffset, y + 30,
                    0.0F, 160.0F, 109, 32, 256, 256);
            float ratio = Mth.clamp((float) archaicRaidAlive / (float) archaicRaidTotal, 0.0F, 1.0F);
            graphics.blit(BARS_LOCATION, x + raidBarXOffset, y + 30,
                    0.0F, 128.0F, (int) (109.0F * ratio), 32, 256, 256);
        }

        if (values.getOrDefault("hasChargedArchaics", 0) == 1) {
            float yOffset = Mth.sin(time * 0.075F) * 4.0F;
            graphics.pose().pushPose();
            graphics.pose().translate(0.0F, yOffset, 0.0F);
            graphics.blit(BARS_LOCATION, x, y, 0.0F, 96.0F, 192, 32, 256, 256);
            graphics.pose().popPose();
        }

        Component bossName = event.getBossEvent().getName();
        int nameWidth = minecraft.font.width(bossName);
        int nameX = x + (186 - nameWidth) / 2;
        int nameY = y;
        int mainColor = 0xFF40FFFB;
        int outlineColor = 0xFF041E86;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx != 0 || dy != 0) {
                    graphics.drawString(minecraft.font, bossName, nameX + dx, nameY + dy, outlineColor);
                }
            }
        }
        graphics.drawString(minecraft.font, bossName, nameX, nameY, mainColor);
        event.setIncrement(42);
    }

    private BossbarRenderers() { }
}
