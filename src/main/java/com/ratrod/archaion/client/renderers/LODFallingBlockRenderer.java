package com.ratrod.archaion.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.ratrod.archaion.entities.effect.LODFallingBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * 1.20.1 backport of Archaion's original Last of Deepslate falling-block renderer.
 * The launched terrain chunk keeps the original 20 degrees/tick X-axis tumble.
 */
public class LODFallingBlockRenderer extends EntityRenderer<LODFallingBlock> {
    public LODFallingBlockRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.5F;
    }

    @Override
    public void render(LODFallingBlock entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        BlockState state = entity.getBlockState();
        if (state.getRenderShape() == RenderShape.MODEL) {
            float rotation = entity.tickCount * 20.0F;
            poseStack.pushPose();
            poseStack.mulPose(Axis.XP.rotationDegrees(rotation));
            poseStack.translate(-0.5F, 0.0F, -0.5F);
            Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
                    state, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
        }
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Nullable
    @Override
    public ResourceLocation getTextureLocation(LODFallingBlock entity) {
        return null;
    }
}
