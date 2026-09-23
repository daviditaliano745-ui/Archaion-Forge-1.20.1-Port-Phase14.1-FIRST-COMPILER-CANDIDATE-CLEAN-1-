package com.ratrod.archaion.client;

import com.ratrod.archaion.Archaion;
import com.ratrod.archaion.client.model.BraveModel;
import com.ratrod.archaion.client.model.DeepslateSentinelModel;
import com.ratrod.archaion.client.model.GrimorayModel;
import com.ratrod.archaion.client.model.HaunterModel;
import com.ratrod.archaion.client.model.LastOfDeepslateModel;
import com.ratrod.archaion.client.renderers.HologramRenderer;
import com.ratrod.archaion.client.renderers.LODFallingBlockRenderer;
import com.ratrod.archaion.client.renderers.BraveRenderer;
import com.ratrod.archaion.client.renderers.DeepslateSentinelRenderer;
import com.ratrod.archaion.client.renderers.GrimorayRenderer;
import com.ratrod.archaion.client.renderers.HaunterRenderer;
import com.ratrod.archaion.client.renderers.LastOfDeepslateRenderer;
import com.ratrod.archaion.client.renderers.SlatedRenderer;
import com.ratrod.archaion.client.renderers.WightRenderer;
import com.ratrod.archaion.client.renderers.TrialSpawnerRenderer;
import com.ratrod.archaion.client.renderers.TeleporterRenderer;
import com.ratrod.archaion.client.renderers.ThrownEchoMaceRenderer;
import com.ratrod.archaion.item.EchosGraceItem;
import com.ratrod.archaion.client.misc.ClientEchoChargeRequiredTooltip;
import com.ratrod.archaion.client.misc.EchoChargeRequiredTooltip;
import com.ratrod.archaion.registry.ACBlockEntities;
import com.ratrod.archaion.registry.ACEntityTypes;
import com.ratrod.archaion.registry.ACItems;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Archaion.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ACClientSetup {
    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(BraveModel.LAYER_LOCATION, BraveModel::createBodyLayer);
        event.registerLayerDefinition(BraveModel.CHARGED_LAYER_LOCATION, () -> BraveModel.createBodyLayer(new net.minecraft.client.model.geom.builders.CubeDeformation(2.0F)));
        event.registerLayerDefinition(DeepslateSentinelModel.LAYER_LOCATION, DeepslateSentinelModel::createBodyLayer);
        event.registerLayerDefinition(GrimorayModel.LAYER_LOCATION, GrimorayModel::createBodyLayer);
        event.registerLayerDefinition(HaunterModel.LAYER_LOCATION, HaunterModel::createBodyLayer);
        event.registerLayerDefinition(HaunterModel.CHARGED_LAYER_LOCATION, () -> HaunterModel.createBodyLayer(new net.minecraft.client.model.geom.builders.CubeDeformation(2.0F)));
        event.registerLayerDefinition(LastOfDeepslateModel.LAYER_LOCATION, LastOfDeepslateModel::createBodyLayer);
        event.registerLayerDefinition(LastOfDeepslateModel.CHARGED_LAYER_LOCATION, () -> LastOfDeepslateModel.createBodyLayer(new net.minecraft.client.model.geom.builders.CubeDeformation(2.0F)));
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ACBlockEntities.HOLOGRAM.get(), HologramRenderer::new);
        event.registerBlockEntityRenderer(ACBlockEntities.TELEPORTER.get(), TeleporterRenderer::new);
        event.registerBlockEntityRenderer(ACBlockEntities.TRIAL_SPAWNER.get(), TrialSpawnerRenderer::new);

        event.registerEntityRenderer(ACEntityTypes.SLATED.get(), SlatedRenderer::new);
        event.registerEntityRenderer(ACEntityTypes.WIGHT.get(), WightRenderer::new);
        event.registerEntityRenderer(ACEntityTypes.BRAVE.get(), BraveRenderer::new);
        event.registerEntityRenderer(ACEntityTypes.GRIMORAY.get(), GrimorayRenderer::new);
        event.registerEntityRenderer(ACEntityTypes.DEEPSLATE_SENTINEL.get(), DeepslateSentinelRenderer::new);
        event.registerEntityRenderer(ACEntityTypes.HAUNTER.get(), HaunterRenderer::new);
        event.registerEntityRenderer(ACEntityTypes.LAST_OF_DEEPSLATE.get(), LastOfDeepslateRenderer::new);

        // The original falling terrain chunk is a visible tumbling block; other effect-only entities below are intentionally invisible.
        event.registerEntityRenderer(ACEntityTypes.LOD_FALLING_BLOCK.get(), LODFallingBlockRenderer::new);
        event.registerEntityRenderer(ACEntityTypes.LOD_SLAM.get(), InvisibleEntityRenderer::new);
        event.registerEntityRenderer(ACEntityTypes.ECHO_STAR.get(), InvisibleEntityRenderer::new);
        event.registerEntityRenderer(ACEntityTypes.LOD_INTERCEPT_BLAST.get(), InvisibleEntityRenderer::new);
        event.registerEntityRenderer(ACEntityTypes.GRIMORAY_SPELL.get(), InvisibleEntityRenderer::new);
        event.registerEntityRenderer(ACEntityTypes.THROWN_IMPACT_PEARL.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ACEntityTypes.THROWN_ECHO_MACE.get(), ThrownEchoMaceRenderer::new);
    }

    @SubscribeEvent
    public static void registerTooltipComponents(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(EchoChargeRequiredTooltip.class, ClientEchoChargeRequiredTooltip::new);
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemProperties.register(ACItems.ECHOS_GRACE.get(), new ResourceLocation("pulling"),
                    (stack, level, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
            ItemProperties.register(ACItems.ECHOS_GRACE.get(), Archaion.prefix("echos_grace_pull"),
                    (stack, level, entity, seed) -> {
                        if (entity == null || !entity.isUsingItem() || entity.getUseItem() != stack) return 0.0F;
                        int used = stack.getUseDuration() - entity.getUseItemRemainingTicks();
                        return EchosGraceItem.getPowerForTime(used, stack);
                    });
        });
    }

    private ACClientSetup() { }
}
