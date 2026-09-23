package com.ratrod.archaion.client.renderers;

import com.ratrod.archaion.Archaion;
import com.ratrod.archaion.client.model.LastOfDeepslateModel;
import com.ratrod.archaion.client.renderers.layer.ChargedSwirlLayer;
import com.ratrod.archaion.client.renderers.layer.EmissiveLayer;
import com.ratrod.archaion.entities.LastOfDeepslate;
import com.ratrod.archaion.entities.SleepingState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public final class LastOfDeepslateRenderer extends MobRenderer<LastOfDeepslate, LastOfDeepslateModel> {
    private static final ResourceLocation TEXTURE = Archaion.prefix("textures/entity/last_of_deepslate.png");
    private static final ResourceLocation GLOW = Archaion.prefix("textures/entity/last_of_deepslate_glow.png");
    private static final ResourceLocation TEXTURE_P1 = Archaion.prefix("textures/entity/last_of_deepslate_p1.png");
    private static final ResourceLocation GLOW_P1 = Archaion.prefix("textures/entity/last_of_deepslate_glow_p1.png");
    private static final ResourceLocation TEXTURE_P2 = Archaion.prefix("textures/entity/last_of_deepslate_p2.png");
    private static final ResourceLocation GLOW_P2 = Archaion.prefix("textures/entity/last_of_deepslate_glow_p2.png");
    private static final ResourceLocation CHARGED = Archaion.prefix("textures/entity/last_of_deepslate_charged.png");

    public LastOfDeepslateRenderer(EntityRendererProvider.Context context) {
        super(context, new LastOfDeepslateModel(context.bakeLayer(LastOfDeepslateModel.LAYER_LOCATION)), 5.0F);
        addLayer(new EmissiveLayer<>(this, e -> switch (e.getPhase()) {
            case 2 -> GLOW_P2;
            case 1 -> GLOW_P1;
            default -> GLOW;
        }, e -> e.getSleepingState() == SleepingState.SLEEPING ? 0.0F : Mth.clamp(1.0F + Mth.sin(e.tickCount * 0.25F), 0.0F, 1.0F)));
        addLayer(new ChargedSwirlLayer<>(this, CHARGED, 0.005F,
                new LastOfDeepslateModel(context.bakeLayer(LastOfDeepslateModel.CHARGED_LAYER_LOCATION))));
    }

    @Override protected float getFlipDegrees(LastOfDeepslate entity) { return 0.0F; }
    @Override public ResourceLocation getTextureLocation(LastOfDeepslate entity) {
        return switch (entity.getPhase()) {
            case 2 -> TEXTURE_P2;
            case 1 -> TEXTURE_P1;
            default -> TEXTURE;
        };
    }
    @Override protected int getBlockLightLevel(LastOfDeepslate entity, BlockPos pos) {
        return entity.getSleepingState() == SleepingState.SLEEPING ? 5 : 10;
    }
}
