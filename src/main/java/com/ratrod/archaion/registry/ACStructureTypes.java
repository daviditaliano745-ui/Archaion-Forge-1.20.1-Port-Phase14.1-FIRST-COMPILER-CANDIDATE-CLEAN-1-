package com.ratrod.archaion.registry;

import com.ratrod.archaion.Archaion;
import com.ratrod.archaion.worldgen.AncientKeepStructure;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ACStructureTypes {
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, Archaion.MODID);

    public static final RegistryObject<StructureType<AncientKeepStructure>> ANCIENT_KEEP =
            STRUCTURE_TYPES.register("ancient_keep", () -> () -> AncientKeepStructure.CODEC);

    private ACStructureTypes() {
    }
}
