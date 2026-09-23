package com.ratrod.archaion.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.ratrod.archaion.block.HologramBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/** Forge 1.20.1 rendering port for the Ancient Keep hologram text. */
public final class HologramRenderer implements BlockEntityRenderer<HologramBlockEntity> {
    private final Font font;

    public HologramRenderer(BlockEntityRendererProvider.Context context) {
        this.font = context.getFont();
    }

    @Override
    public void render(HologramBlockEntity entity, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        String key = entity.getText();
        if (key == null || key.isEmpty()) return;

        List<FormattedCharSequence> lines = font.split(Component.translatable(key), 160);
        if (lines.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        float bobY = 1.5F + Mth.sin((entity.clientTicks + partialTick) * 0.1F) * 0.1F;
        Vec3 textPos = Vec3.atCenterOf(entity.getBlockPos()).add(0.0D, bobY - 0.5D, 0.0D);
        float distance = (float) mc.player.position().distanceTo(textPos);
        float alpha = Mth.clamp((6.0F - distance) / 4.0F, 0.0F, 1.0F);
        if (alpha <= 0.0F) return;

        int textColor = applyAlpha(entity.getTextColor(), alpha);
        int outlineColor = applyAlpha(0x00023238, alpha);
        float maxWidth = 0.0F;
        for (FormattedCharSequence line : lines) maxWidth = Math.max(maxWidth, font.width(line));
        float y = -(lines.size() * 9.0F);

        poseStack.pushPose();
        poseStack.translate(0.5D, bobY, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(-mc.gameRenderer.getMainCamera().getYRot() + 180.0F));
        poseStack.translate(0.0D, 0.0D, 0.01D);
        poseStack.scale(0.02F, -0.02F, 0.02F);
        for (FormattedCharSequence line : lines) {
            float x = -maxWidth * 0.5F + (maxWidth - font.width(line)) * 0.5F;
            font.drawInBatch8xOutline(line, x, y, textColor, outlineColor,
                    poseStack.last().pose(), buffers, LightTexture.FULL_BRIGHT);
            y += 9.0F;
        }
        poseStack.popPose();
    }

    private static int applyAlpha(int color, float alpha) {
        int a = (int) (255.0F * Mth.clamp(alpha, 0.0F, 1.0F));
        return color & 0x00FFFFFF | a << 24;
    }

    @Override public boolean shouldRenderOffScreen(HologramBlockEntity entity) { return true; }
    @Override public int getViewDistance() { return 128; }
    @Override public boolean shouldRender(HologramBlockEntity entity, Vec3 cameraPos) { return true; }
}
