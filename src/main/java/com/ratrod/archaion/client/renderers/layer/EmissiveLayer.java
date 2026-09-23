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

import java.util.function.Function;
import java.util.function.ToDoubleFunction;

/** Lightweight Forge 1.20.1 equivalent of Archaion's emissive render layer. */
public final class EmissiveLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private final Function<T, ResourceLocation> texture;
    private final ToDoubleFunction<T> alpha;

    public EmissiveLayer(RenderLayerParent<T, M> parent, ResourceLocation texture, ToDoubleFunction<T> alpha) {
        this(parent, e -> texture, alpha);
    }

    public EmissiveLayer(RenderLayerParent<T, M> parent, Function<T, ResourceLocation> texture, ToDoubleFunction<T> alpha) {
        super(parent);
        this.texture = texture;
        this.alpha = alpha;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (entity.isInvisible()) return;
        float a = (float)Math.max(0.0D, Math.min(1.0D, alpha.applyAsDouble(entity)));
        if (a <= 0.0F) return;
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucentEmissive(texture.apply(entity)));
        getParentModel().renderToBuffer(poseStack, consumer, 0xF000F0, OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, a);
    }
}
