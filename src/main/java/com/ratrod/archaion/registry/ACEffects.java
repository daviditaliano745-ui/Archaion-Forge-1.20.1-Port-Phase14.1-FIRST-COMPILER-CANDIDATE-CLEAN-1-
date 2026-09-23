package com.ratrod.archaion.registry;

import com.ratrod.archaion.Archaion;
import com.ratrod.archaion.effect.ArmorBreakEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ACEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, Archaion.MODID);
    public static final RegistryObject<MobEffect> ARMOR_BREAK = MOB_EFFECTS.register("armor_break", () -> new ArmorBreakEffect(MobEffectCategory.HARMFUL, 4868682));
    private ACEffects() {}
}
