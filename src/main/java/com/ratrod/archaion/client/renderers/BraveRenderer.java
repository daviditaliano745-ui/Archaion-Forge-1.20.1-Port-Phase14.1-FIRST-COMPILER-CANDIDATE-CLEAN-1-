package com.ratrod.archaion.client.renderers;

import com.ratrod.archaion.Archaion;
import com.ratrod.archaion.client.model.BraveModel;
import com.ratrod.archaion.client.renderers.layer.ChargedSwirlLayer;
import com.ratrod.archaion.client.renderers.layer.EmissiveLayer;
import com.ratrod.archaion.entities.Brave;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public final class BraveRenderer extends MobRenderer<Brave, BraveModel> {
    private static final ResourceLocation TEXTURE = Archaion.prefix("textures/entity/brave.png");
    private static final ResourceLocation GLOW = Archaion.prefix("textures/entity/brave_glow.png");
    private static final ResourceLocation CHARGED = Archaion.prefix("textures/entity/brave_charged.png");

    public BraveRenderer(EntityRendererProvider.Context context) {
        super(context, new BraveModel(context.bakeLayer(BraveModel.LAYER_LOCATION)), 0.5F);
        addLayer(new EmissiveLayer<>(this, GLOW, e -> Mth.clamp(1.0F + Mth.sin(e.tickCount * 0.3F), 0.0F, 1.0F)));
        addLayer(new ChargedSwirlLayer<>(this, CHARGED, 0.01F,
                new BraveModel(context.bakeLayer(BraveModel.CHARGED_LAYER_LOCATION))));
    }
    @Override public ResourceLocation getTextureLocation(Brave entity) { return TEXTURE; }
}
