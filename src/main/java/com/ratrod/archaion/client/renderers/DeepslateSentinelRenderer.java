package com.ratrod.archaion.client.renderers;

import com.ratrod.archaion.Archaion;
import com.ratrod.archaion.client.model.DeepslateSentinelModel;
import com.ratrod.archaion.client.renderers.layer.ChargedSwirlLayer;
import com.ratrod.archaion.entities.DeepslateSentinel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class DeepslateSentinelRenderer extends MobRenderer<DeepslateSentinel, DeepslateSentinelModel> {
    private static final ResourceLocation TEXTURE = Archaion.prefix("textures/entity/deepslate_sentinel.png");
    private static final ResourceLocation CHARGED = Archaion.prefix("textures/entity/last_of_deepslate_charged.png");
    public DeepslateSentinelRenderer(EntityRendererProvider.Context context) {
        super(context, new DeepslateSentinelModel(context.bakeLayer(DeepslateSentinelModel.LAYER_LOCATION)), 0.5F);
        addLayer(new ChargedSwirlLayer<>(this, CHARGED, 0.01F));
    }
    @Override public ResourceLocation getTextureLocation(DeepslateSentinel entity) { return TEXTURE; }
}
