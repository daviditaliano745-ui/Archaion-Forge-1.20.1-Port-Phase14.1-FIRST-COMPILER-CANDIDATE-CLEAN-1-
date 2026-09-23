package com.ratrod.archaion.item;

import com.ratrod.archaion.entities.projectile.EchoStarProjectile;
import com.ratrod.archaion.registry.ACSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/** 1.20.1 backport of Echo's Grace, including bow + crossbow-style enchant behaviour. */
public class EchosGraceItem extends BowItem {
    public static final int MAX_DRAW_DURATION = 20;
    public static final int DEFAULT_RANGE = 15;

    public EchosGraceItem(Properties properties) { super(properties); }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity living, int timeLeft) {
        if (!(living instanceof Player player)) return;
        int used = getUseDuration(stack) - timeLeft;
        if (used < 0) return;
        float power = getPowerForTime(used, stack);
        if (power < 0.1F) return;

        if (level instanceof ServerLevel server) {
            int multishot = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MULTISHOT, stack);
            int projectiles = multishot > 0 ? 3 : 1;
            int powerLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, stack);
            for (int i = 0; i < projectiles; i++) {
                float yawOffset = projectiles == 1 ? 0.0F : (i - 1) * 10.0F;
                EchoStarProjectile projectile = new EchoStarProjectile(server, player);
                projectile.setBaseDamage(12.0F);
                projectile.setPowerBonus(powerLevel);
                projectile.shootFromRotation(player, player.getXRot(), player.getYRot() + yawOffset, 0.0F, power * 2.0F, 1.0F);
                server.addFreshEntity(projectile);
            }
            stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(player.getUsedItemHand()));
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(), ACSounds.LOD_SHOOT.get(), player.getSoundSource(),
                1.0F, 1.0F / (level.random.nextFloat() * 0.4F + 1.2F) + power * 0.5F);
        player.awardStat(Stats.ITEM_USED.get(this));
    }

    public static float getPowerForTime(int useTicks, ItemStack stack) {
        int quickCharge = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.QUICK_CHARGE, stack);
        float chargeTime = Math.max(5.0F, MAX_DRAW_DURATION - quickCharge * 5.0F);
        float f = useTicks / chargeTime;
        f = (f * f + f * 2.0F) / 3.0F;
        return Mth.clamp(f, 0.0F, 1.0F);
    }

    @Override public int getUseDuration(ItemStack stack) { return 72000; }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }
    @Override public int getEnchantmentValue() { return 15; }
    @Override public boolean isValidRepairItem(ItemStack stack, ItemStack ingredient) { return ingredient.is(Items.ECHO_SHARD); }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return super.canApplyAtEnchantingTable(stack, enchantment)
                || enchantment == Enchantments.MULTISHOT
                || enchantment == Enchantments.QUICK_CHARGE
                || enchantment == Enchantments.PIERCING;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.archaion.echos_grace.desc",
                Component.translatable("item.archaion.echos_grace.keyword.echo_charges").withStyle(ChatFormatting.AQUA))
                .withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(CommonComponents.EMPTY);
    }
}
