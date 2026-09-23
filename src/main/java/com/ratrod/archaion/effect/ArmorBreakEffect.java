package com.ratrod.archaion.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/** 1.20.1 equivalent of the 1.21 effect: -5% armor and toughness per level. */
public class ArmorBreakEffect extends MobEffect {
    public ArmorBreakEffect(MobEffectCategory category, int color) {
        super(category, color);
        addAttributeModifier(Attributes.ARMOR, "9D45A7C9-342C-45CE-9E40-7A22F4F816A1", -0.05D, AttributeModifier.Operation.MULTIPLY_TOTAL);
        addAttributeModifier(Attributes.ARMOR_TOUGHNESS, "8BE7D3F4-9826-4A1F-8F7C-520F52D58142", -0.05D, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }
}
