package com.ratrod.archaion.registry;

import com.mojang.serialization.Codec;
import com.ratrod.archaion.Archaion;
import com.ratrod.archaion.loot.AddLootTableModifier;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ACLootModifiers {
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, Archaion.MODID);

    public static final RegistryObject<Codec<AddLootTableModifier>> ADD_LOOT_TABLE =
            SERIALIZERS.register("add_loot_table", () -> AddLootTableModifier.CODEC);

    private ACLootModifiers() {}
}
