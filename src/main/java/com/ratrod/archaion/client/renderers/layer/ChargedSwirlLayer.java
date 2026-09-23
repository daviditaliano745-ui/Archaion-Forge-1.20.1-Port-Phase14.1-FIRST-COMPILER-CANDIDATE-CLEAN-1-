package com.ratrod.archaion.client.renderers.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PowerableMob;

/** Shared charged-overlay layer that keeps the port's event-driven entity architecture. */
public final class ChargedSwirlLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private final ResourceLocation texture;
    private final float xSpeed;
    private final M chargedModel;

    public ChargedSwirlLayer(RenderLayerParent<T, M> parent, ResourceLocation texture, float xSpeed) {
        this(parent, texture, xSpeed, null);
    }

    public ChargedSwirlLayer(RenderLayerParent<T, M> parent, ResourceLocation texture, float xSpeed, M chargedModel) {
        super(parent);
        this.texture = texture;
        this.xSpeed = xSpeed;
        this.chargedModel = chargedModel;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffers, int packedLight, T entity,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (!(entity instanceof PowerableMob powerable) || !powerable.isPowered() || entity.isInvisible()) return;
        float time = entity.tickCount + partialTick;
        VertexConsumer consumer = buffers.getBuffer(RenderType.energySwirl(texture, time * xSpeed, time * 0.01F));
        M model = chargedModel != null ? chargedModel : getParentModel();
        if (chargedModel != null) {
            getParentModel().copyPropertiesTo(chargedModel);
            chargedModel.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTick);
            chargedModel.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        }
        model.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY,
                0.65F, 0.65F, 0.65F, 1.0F);
    }
}
