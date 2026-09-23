package com.ratrod.archaion.client.renderers;

import com.ratrod.archaion.Archaion;
import com.ratrod.archaion.client.renderers.layer.ChargedSwirlLayer;
import com.ratrod.archaion.client.renderers.layer.EmissiveLayer;
import com.ratrod.archaion.entities.Wight;
import net.minecraft.client.model.SkeletonModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;

/** Type-safe Forge 1.20.1 version of Archaion's Wight skeleton renderer. */
public final class WightRenderer extends HumanoidMobRenderer<Wight, SkeletonModel<Wight>> {
    private static final ResourceLocation TEXTURE = Archaion.prefix("textures/entity/wight.png");
    private static final ResourceLocation EYES = Archaion.prefix("textures/entity/wight_eyes.png");
    private static final ResourceLocation CHARGED = Archaion.prefix("textures/entity/brave_charged.png");

    public WightRenderer(EntityRendererProvider.Context context) {
        super(context, new SkeletonModel<>(context.bakeLayer(ModelLayers.SKELETON)), 0.5F);
        addLayer(new HumanoidArmorLayer<>(this,
                new SkeletonModel<>(context.bakeLayer(ModelLayers.SKELETON_INNER_ARMOR)),
                new SkeletonModel<>(context.bakeLayer(ModelLayers.SKELETON_OUTER_ARMOR)),
                context.getModelManager()));
        addLayer(new EmissiveLayer<>(this, EYES, e -> 1.0F));
        addLayer(new ChargedSwirlLayer<>(this, CHARGED, 0.01F));
    }

    @Override
    public ResourceLocation getTextureLocation(Wight entity) {
        return TEXTURE;
    }
}
