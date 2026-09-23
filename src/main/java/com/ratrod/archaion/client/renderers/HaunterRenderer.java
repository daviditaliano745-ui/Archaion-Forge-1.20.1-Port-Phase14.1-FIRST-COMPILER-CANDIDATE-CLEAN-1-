package com.ratrod.archaion.client.renderers;

import com.ratrod.archaion.Archaion;
import com.ratrod.archaion.client.model.HaunterModel;
import com.ratrod.archaion.client.renderers.layer.ChargedSwirlLayer;
import com.ratrod.archaion.client.renderers.layer.EmissiveLayer;
import com.ratrod.archaion.entities.Haunter;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public final class HaunterRenderer extends MobRenderer<Haunter, HaunterModel> {
    private static final ResourceLocation TEXTURE = Archaion.prefix("textures/entity/haunter.png");
    private static final ResourceLocation CHARGED = Archaion.prefix("textures/entity/brave_charged.png");
    public HaunterRenderer(EntityRendererProvider.Context context) {
        super(context, new HaunterModel(context.bakeLayer(HaunterModel.LAYER_LOCATION)), 0.4F);
        addLayer(new EmissiveLayer<>(this, TEXTURE, e -> 1.0F));
        addLayer(new ChargedSwirlLayer<>(this, CHARGED, 0.01F,
                new HaunterModel(context.bakeLayer(HaunterModel.CHARGED_LAYER_LOCATION))));
    }
    @Override public ResourceLocation getTextureLocation(Haunter entity) { return TEXTURE; }
    @Override protected float getWhiteOverlayProgress(Haunter entity, float partialTicks) {
        float value = 0.75F + Mth.sin(entity.tickCount) * 0.25F;
        return entity.isSwelling() ? Mth.clamp(value, 0.5F, 1.0F) : 0.0F;
    }
}
