package com.ratrod.archaion.registry;

import com.ratrod.archaion.Archaion;
import com.ratrod.archaion.item.ImpactPearlItem;
import com.ratrod.archaion.item.EchoMaceItem;
import com.ratrod.archaion.item.EchosGraceItem;
import com.ratrod.archaion.item.EchoChargeItem;
import com.ratrod.archaion.item.ACSmithingTemplateItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Phase-1 item registry. Registry names are kept identical to the 1.21.1 build.
 * Registry names are preserved from the 1.21.1 build. Complex 1.21 data-component
 * behaviour is reimplemented with 1.20.1 item/entity mechanics where required.
 */
public final class ACItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Archaion.MODID);

    public static final RegistryObject<Item> ECHO_KEY = simple("echo_key");
    public static final RegistryObject<Item> BRAVE_ROD = simple("brave_rod");
    public static final RegistryObject<Item> BRAVE_ESSENCE = simple("brave_essence");
    public static final RegistryObject<Item> IMPACT_PEARL = ITEMS.register("impact_pearl", () -> new ImpactPearlItem(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> ECHO_CHARGE = ITEMS.register("echo_charge", () -> new EchoChargeItem(new Item.Properties()));
    public static final RegistryObject<Item> ECHO_MACE_UPGRADE_SMITHING_TEMPLATE = ITEMS.register("echo_mace_upgrade_smithing_template", () -> ACSmithingTemplateItem.maceUpgrade(new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> ECHOS_GRACE_UPGRADE_SMITHING_TEMPLATE = ITEMS.register("echos_grace_upgrade_smithing_template", () -> ACSmithingTemplateItem.echosGraceUpgrade(new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> ECHO_MACE = ITEMS.register("echo_mace", () -> new EchoMaceItem(new Item.Properties().rarity(Rarity.EPIC).durability(250)));
    public static final RegistryObject<Item> ECHOS_GRACE = ITEMS.register("echos_grace", () -> new EchosGraceItem(new Item.Properties().rarity(Rarity.EPIC).durability(512)));

    // Ported mobs use real Forge spawn eggs; colors are deferred to the visual parity pass.
    public static final RegistryObject<Item> SLATED_SPAWN_EGG = ITEMS.register("slated_spawn_egg", () ->
            new ForgeSpawnEggItem(ACEntityTypes.SLATED, -1, -1, new Item.Properties()));
    public static final RegistryObject<Item> WIGHT_SPAWN_EGG = ITEMS.register("wight_spawn_egg", () ->
            new ForgeSpawnEggItem(ACEntityTypes.WIGHT, -1, -1, new Item.Properties()));
    public static final RegistryObject<Item> BRAVE_SPAWN_EGG = ITEMS.register("brave_spawn_egg", () ->
            new ForgeSpawnEggItem(ACEntityTypes.BRAVE, -1, -1, new Item.Properties()));
    public static final RegistryObject<Item> DEEPSLATE_SENTINEL_SPAWN_EGG = ITEMS.register("deepslate_sentinel_spawn_egg", () ->
            new ForgeSpawnEggItem(ACEntityTypes.DEEPSLATE_SENTINEL, -1, -1, new Item.Properties()));
    public static final RegistryObject<Item> GRIMORAY_SPAWN_EGG = ITEMS.register("grimoray_spawn_egg", () ->
            new ForgeSpawnEggItem(ACEntityTypes.GRIMORAY, -1, -1, new Item.Properties()));
    public static final RegistryObject<Item> HAUNTER_SPAWN_EGG = ITEMS.register("haunter_spawn_egg", () ->
            new ForgeSpawnEggItem(ACEntityTypes.HAUNTER, -1, -1, new Item.Properties()));
    public static final RegistryObject<Item> LAST_OF_DEEPSLATE_SPAWN_EGG = ITEMS.register("last_of_deepslate_spawn_egg", () ->
            new ForgeSpawnEggItem(ACEntityTypes.LAST_OF_DEEPSLATE, -1, -1, new Item.Properties()));

    private static RegistryObject<Item> simple(String name) {
        return ITEMS.register(name, () -> new Item(new Item.Properties()));
    }

    private ACItems() {}
}
