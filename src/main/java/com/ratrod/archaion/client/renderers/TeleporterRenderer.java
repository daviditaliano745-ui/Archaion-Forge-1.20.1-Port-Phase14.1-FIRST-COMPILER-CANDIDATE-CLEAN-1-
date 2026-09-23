package com.ratrod.archaion.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.ratrod.archaion.Archaion;
import com.ratrod.archaion.block.TeleporterBlock;
import com.ratrod.archaion.block.TeleporterBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.text.DecimalFormat;

/** Forge 1.20.1 renderer for the Ancient Keep teleporter beam/cooldown readout. */
public final class TeleporterRenderer implements BlockEntityRenderer<TeleporterBlockEntity> {
    private static final ResourceLocation BEAM_LOCATION = Archaion.prefix("textures/entity/teleporter_beam.png");

    public TeleporterRenderer(BlockEntityRendererProvider.Context context) { }

    @Override
    public void render(TeleporterBlockEntity entity, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        int argb = entity.getBlockState().getValue(TeleporterBlock.COLOR).argb();
        float ticks = entity.tickCount + partialTick;
        int height = Math.max(1, entity.maxHeight);
        int cooldown = entity.getCooldownTicks();

        if (cooldown > 0) {
            Font font = Minecraft.getInstance().font;
            float bobY = 1.5F + Mth.sin(ticks * 0.1F) * 0.1F;
            Component text = Component.literal(new DecimalFormat("0.0").format(cooldown / 20.0F) + "s");
            float x = -font.width(text) * 0.5F;
            poseStack.pushPose();
            poseStack.translate(0.5D, bobY, 0.5D);
            poseStack.mulPose(Axis.YP.rotationDegrees(-Minecraft.getInstance().gameRenderer.getMainCamera().getYRot() + 180.0F));
            poseStack.translate(0.0D, 0.0D, 0.01D);
            poseStack.scale(0.05F, -0.05F, 0.05F);
            font.drawInBatch8xOutline(text.getVisualOrderText(), x, 0.0F, argb, 0xFF023238,
                    poseStack.last().pose(), buffers, LightTexture.FULL_BRIGHT);
            poseStack.popPose();
            return;
        }

        float r = ((argb >> 16) & 255) / 255.0F;
        float g = ((argb >> 8) & 255) / 255.0F;
        float b = (argb & 255) / 255.0F;
        float[] rgb = {r, g, b};
        float inner = 0.25F + Mth.cos(ticks * 0.1F) * 0.05F;
        float outer = 0.30F + Mth.sin(ticks * 0.1F) * 0.10F;
        BeaconRenderer.renderBeaconBeam(poseStack, buffers, BEAM_LOCATION,
                1.0F, ticks * 2.0F, 0L, 0, height, rgb, inner, outer);
    }

    @Override public boolean shouldRenderOffScreen(TeleporterBlockEntity entity) { return true; }
    @Override public int getViewDistance() { return 128; }
    @Override public boolean shouldRender(TeleporterBlockEntity entity, Vec3 cameraPos) { return true; }
}
