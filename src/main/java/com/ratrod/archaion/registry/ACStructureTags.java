package com.ratrod.archaion.registry;

import com.ratrod.archaion.Archaion;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.structure.Structure;

/** Structure tags used by runtime systems that need to identify an Ancient Keep. */
public final class ACStructureTags {
    public static final TagKey<Structure> ON_ANCIENT_KEEP_MAPS =
            TagKey.create(Registries.STRUCTURE, Archaion.prefix("on_ancient_keep_maps"));

    private ACStructureTags() { }
}
