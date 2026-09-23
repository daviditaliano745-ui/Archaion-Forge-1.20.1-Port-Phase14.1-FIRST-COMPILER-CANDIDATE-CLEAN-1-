package com.ratrod.archaion.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.ratrod.archaion.api.trial.ACTrialSpawnerBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

/** 1.20.1 equivalent of Archaion's 1.21 TrialSpawner display-entity renderer. */
public final class TrialSpawnerRenderer implements BlockEntityRenderer<ACTrialSpawnerBlockEntity> {
    private final EntityRenderDispatcher entityRenderer;

    public TrialSpawnerRenderer(BlockEntityRendererProvider.Context context) {
        this.entityRenderer = context.getEntityRenderer();
    }

    @Override
    public void render(ACTrialSpawnerBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Entity entity = blockEntity.getOrCreateClientDisplayEntity();
        if (entity == null) return;

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        float scale = 0.53125F;
        float maxSize = Math.max(entity.getBbWidth(), entity.getBbHeight());
        if (maxSize > 1.0F) scale /= maxSize;
        poseStack.translate(0.0D, 0.4D, 0.0D);
        double spin = Mth.lerp((double)partialTick, blockEntity.getClientOldSpin(), blockEntity.getClientSpin());
        poseStack.mulPose(Axis.YP.rotationDegrees((float)spin * 10.0F));
        poseStack.translate(0.0D, -0.2D, 0.0D);
        poseStack.mulPose(Axis.XP.rotationDegrees(-30.0F));
        poseStack.scale(scale, scale, scale);
        entityRenderer.render(entity, 0.0D, 0.0D, 0.0D, 0.0F, partialTick, poseStack, buffer, packedLight);
        poseStack.popPose();
    }
}
