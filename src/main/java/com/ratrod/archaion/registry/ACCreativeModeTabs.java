package com.ratrod.archaion.registry;

import com.ratrod.archaion.Archaion;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ACCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Archaion.MODID);
    public static final RegistryObject<CreativeModeTab> ARCHAION_TAB = CREATIVE_MODE_TABS.register("archaion_tab", () ->
            CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ACItems.ECHO_MACE.get()))
                    .title(Component.translatable("creativetab.archaion_tab"))
                    .displayItems((parameters, output) -> ACItems.ITEMS.getEntries().forEach(item -> output.accept(item.get())))
                    .build());
    private ACCreativeModeTabs() {}
}
