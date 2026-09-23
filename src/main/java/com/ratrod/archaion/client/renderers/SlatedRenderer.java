package com.ratrod.archaion.client.renderers;

import com.ratrod.archaion.Archaion;
import com.ratrod.archaion.client.renderers.layer.ChargedSwirlLayer;
import com.ratrod.archaion.client.renderers.layer.EmissiveLayer;
import com.ratrod.archaion.entities.Slated;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AbstractZombieRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/** Native 1.20.1 equivalent of Archaion's Slated renderer. */
public final class SlatedRenderer extends AbstractZombieRenderer<Slated, ZombieModel<Slated>> {
    private static final ResourceLocation TEXTURE = Archaion.prefix("textures/entity/slated.png");
    private static final ResourceLocation GLOW = Archaion.prefix("textures/entity/slated_glow.png");
    private static final ResourceLocation CHARGED = Archaion.prefix("textures/entity/brave_charged.png");

    public SlatedRenderer(EntityRendererProvider.Context context) {
        super(context,
                new ZombieModel<>(context.bakeLayer(ModelLayers.ZOMBIE)),
                new ZombieModel<>(context.bakeLayer(ModelLayers.ZOMBIE_INNER_ARMOR)),
                new ZombieModel<>(context.bakeLayer(ModelLayers.ZOMBIE_OUTER_ARMOR)));
        addLayer(new EmissiveLayer<>(this, GLOW, e -> 1.0F));
        addLayer(new ChargedSwirlLayer<>(this, CHARGED, 0.01F));
    }

    @Override
    public ResourceLocation getTextureLocation(Slated entity) {
        return TEXTURE;
    }
}
