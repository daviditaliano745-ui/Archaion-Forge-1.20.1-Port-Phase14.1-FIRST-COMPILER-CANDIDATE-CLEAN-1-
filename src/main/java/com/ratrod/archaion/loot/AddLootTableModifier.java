package com.ratrod.archaion.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

/**
 * Forge 1.20.1 replacement for Archaion's NeoForge add-loot-table modifier.
 *
 * The secondary table is rolled with getRandomItemsRaw on purpose: this is
 * already inside a GLM, and applying GLMs again while resolving the added
 * table could recursively re-run the Ancient City modifier.
 */
public final class AddLootTableModifier extends LootModifier {
    public static final Codec<AddLootTableModifier> CODEC = RecordCodecBuilder.create(inst ->
            LootModifier.codecStart(inst)
                    .and(ResourceLocation.CODEC.fieldOf("loot_table").forGetter(modifier -> modifier.lootTable))
                    .apply(inst, AddLootTableModifier::new)
    );

    private final ResourceLocation lootTable;

    public AddLootTableModifier(LootItemCondition[] conditions, ResourceLocation lootTable) {
        super(conditions);
        this.lootTable = lootTable;
    }

    @NotNull
    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        LootTable table = context.getLevel().getServer().getLootData().getLootTable(lootTable);
        table.getRandomItemsRaw(context, generatedLoot::add);
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
