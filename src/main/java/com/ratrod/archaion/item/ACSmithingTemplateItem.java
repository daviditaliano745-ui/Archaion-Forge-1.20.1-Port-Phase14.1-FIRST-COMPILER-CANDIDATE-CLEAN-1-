package com.ratrod.archaion.item;

import com.ratrod.archaion.Archaion;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SmithingTemplateItem;

import java.util.List;

/** Forge 1.20.1 equivalent of Archaion's two custom upgrade smithing templates. */
public class ACSmithingTemplateItem extends SmithingTemplateItem {
    public ACSmithingTemplateItem(Component appliesTo, Component ingredients, Component upgradeDescription,
                                  Component baseSlotDescription, Component additionsSlotDescription,
                                  List<ResourceLocation> baseSlotEmptyIcons,
                                  List<ResourceLocation> additionalSlotEmptyIcons) {
        super(appliesTo, ingredients, upgradeDescription, baseSlotDescription, additionsSlotDescription,
                baseSlotEmptyIcons, additionalSlotEmptyIcons);
    }

    public static ACSmithingTemplateItem maceUpgrade(Item.Properties ignoredProperties) {
        return new ACSmithingTemplateItem(
                description("mace_upgrade", "applies_to"),
                description("mace_upgrade", "ingredients"),
                description("mace_upgrade", "upgrade_description"),
                description("mace_upgrade", "base_slot_description"),
                description("mace_upgrade", "additions_slot_description"),
                List.of(Archaion.prefix("item/empty_slot_mace")),
                List.of(Archaion.prefix("item/empty_slot_echo_shard"))
        );
    }

    public static ACSmithingTemplateItem echosGraceUpgrade(Item.Properties ignoredProperties) {
        return new ACSmithingTemplateItem(
                description("echos_grace_upgrade", "applies_to"),
                description("echos_grace_upgrade", "ingredients"),
                description("echos_grace_upgrade", "upgrade_description"),
                description("echos_grace_upgrade", "base_slot_description"),
                description("echos_grace_upgrade", "additions_slot_description"),
                List.of(Archaion.prefix("item/empty_slot_bow")),
                List.of(Archaion.prefix("item/empty_slot_echo_charge"))
        );
    }

    private static Component description(String upgrade, String suffix) {
        String key = Util.makeDescriptionId("item", Archaion.prefix("smithing_template." + upgrade + "." + suffix));
        return Component.translatable(key).withStyle(ChatFormatting.BLUE).copy();
    }
}
