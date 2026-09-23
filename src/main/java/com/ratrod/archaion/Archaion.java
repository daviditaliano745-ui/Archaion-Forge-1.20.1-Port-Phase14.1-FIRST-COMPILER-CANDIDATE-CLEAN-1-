package com.ratrod.archaion;

import com.mojang.logging.LogUtils;
import com.ratrod.archaion.registry.ACBlocks;
import com.ratrod.archaion.registry.ACBlockEntities;
import com.ratrod.archaion.registry.ACCreativeModeTabs;
import com.ratrod.archaion.registry.ACEffects;
import com.ratrod.archaion.registry.ACEntityTypes;
import com.ratrod.archaion.registry.ACItems;
import com.ratrod.archaion.registry.ACLootModifiers;
import com.ratrod.archaion.registry.ACSounds;
import com.ratrod.archaion.registry.ACStructureTypes;
import com.ratrod.archaion.network.ACNetwork;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import java.util.Locale;

@Mod(Archaion.MODID)
public final class Archaion {
    public static final String MODID = "archaion";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Archaion() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ACBlocks.BLOCKS.register(modBus);
        ACBlockEntities.BLOCK_ENTITIES.register(modBus);
        ACEntityTypes.ENTITY_TYPES.register(modBus);
        ACItems.ITEMS.register(modBus);
        ACLootModifiers.SERIALIZERS.register(modBus);
        ACStructureTypes.STRUCTURE_TYPES.register(modBus);
        ACEffects.MOB_EFFECTS.register(modBus);
        ACSounds.SOUND_EVENTS.register(modBus);
        ACCreativeModeTabs.CREATIVE_MODE_TABS.register(modBus);
        ACNetwork.register();
        modBus.addListener(ACCommonSetup::onRegisterAttributes);
        LOGGER.info("Archaion Forge 1.20.1 port bootstrap loaded");
    }

    public static ResourceLocation prefix(String path) {
        return new ResourceLocation(MODID, path.toLowerCase(Locale.ROOT));
    }
}
