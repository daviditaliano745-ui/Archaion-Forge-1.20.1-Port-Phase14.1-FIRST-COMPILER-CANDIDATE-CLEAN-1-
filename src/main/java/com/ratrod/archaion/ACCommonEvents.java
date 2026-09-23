package com.ratrod.archaion;

import com.ratrod.archaion.registry.ACEffects;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Common Forge events needed by the 1.20.1 port. */
@Mod.EventBusSubscriber(modid = Archaion.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ACCommonEvents {
    private ACCommonEvents() {}

    /**
     * NeoForge 1.21 exposes per-stage damage-reduction modifiers; Forge 1.20.1 does not.
     * Reconstruct the vanilla Resistance + enchantment reduction factors from the target,
     * then weaken only those reductions by the same 7.5% per Armor Break level used by
     * the original mod. Armor/toughness remain handled by the effect's attribute modifiers.
     *
     * This is applied at LivingDamageEvent (the post-reduction Forge hook), which preserves
     * the original final health-damage result for normal non-zero hits. A fully negated hit
     * cannot be reconstructed by the 1.20.1 event API because the pre-magic value is no
     * longer exposed at that point.
     */
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        MobEffectInstance armorBreak = entity.getEffect(ACEffects.ARMOR_BREAK.get());
        if (armorBreak == null || event.getAmount() <= 0.0F) return;

        float reductionScale = Math.max(0.0F, 1.0F - 0.075F * (armorBreak.getAmplifier() + 1));
        float vanillaResistanceFactor = 1.0F;
        float vanillaEnchantFactor = 1.0F;

        if (!event.getSource().is(DamageTypeTags.BYPASSES_EFFECTS)
                && !event.getSource().is(DamageTypeTags.BYPASSES_RESISTANCE)) {
            MobEffectInstance resistance = entity.getEffect(MobEffects.DAMAGE_RESISTANCE);
            if (resistance != null) {
                int remaining = 25 - (resistance.getAmplifier() + 1) * 5;
                vanillaResistanceFactor = Math.max(remaining / 25.0F, 0.0F);
            }
        }

        if (!event.getSource().is(DamageTypeTags.BYPASSES_EFFECTS)
                && !event.getSource().is(DamageTypeTags.BYPASSES_ENCHANTMENTS)) {
            int protection = EnchantmentHelper.getDamageProtection(entity.getArmorSlots(), event.getSource());
            vanillaEnchantFactor = 1.0F - Mth.clamp(protection, 0, 20) / 25.0F;
        }

        float vanillaMagicFactor = vanillaResistanceFactor * vanillaEnchantFactor;
        if (vanillaMagicFactor <= 0.0F) return;

        float weakenedResistanceFactor = 1.0F - (1.0F - vanillaResistanceFactor) * reductionScale;
        float weakenedEnchantFactor = 1.0F - (1.0F - vanillaEnchantFactor) * reductionScale;
        float weakenedMagicFactor = weakenedResistanceFactor * weakenedEnchantFactor;

        event.setAmount(event.getAmount() * (weakenedMagicFactor / vanillaMagicFactor));
    }
}
