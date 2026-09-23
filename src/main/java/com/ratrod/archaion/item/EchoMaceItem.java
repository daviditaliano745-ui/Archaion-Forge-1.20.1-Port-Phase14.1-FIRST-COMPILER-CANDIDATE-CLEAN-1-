package com.ratrod.archaion.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.ratrod.archaion.entities.projectile.ThrownEchoMace;
import com.ratrod.archaion.registry.ACSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Forge 1.20.1 backport of Echo Mace. Vanilla 1.20.1 has no MaceItem/data components,
 * so its 7 damage / -3.2 speed attributes and throw behaviour are implemented directly.
 */
public class EchoMaceItem extends Item {
    private final Multimap<Attribute, AttributeModifier> mainHandModifiers;

    public EchoMaceItem(Properties properties) {
        super(properties);
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", 7.0D, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", -3.2D, AttributeModifier.Operation.ADDITION));
        mainHandModifiers = builder.build();
    }

    @Override public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND ? mainHandModifiers : super.getDefaultAttributeModifiers(slot);
    }
    @Override public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.SPEAR; }
    @Override public int getUseDuration(ItemStack stack) { return 72000; }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) return InteractionResultHolder.fail(stack);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity living, int timeLeft) {
        if (!(living instanceof Player player)) return;
        int used = getUseDuration(stack) - timeLeft;
        if (used < 5) return;
        player.awardStat(Stats.ITEM_USED.get(this));
        if (!(level instanceof ServerLevel server)) return;

        stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(player.getUsedItemHand()));
        ItemStack thrownCopy = stack.copy();
        thrownCopy.setCount(1);
        ThrownEchoMace mace = new ThrownEchoMace(server, player, thrownCopy);
        mace.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.5F, 1.0F);
        server.addFreshEntity(mace);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), ACSounds.ECHO_MACE_THROW.get(), player.getSoundSource(), 1.0F, 1.0F);
        player.getCooldowns().addCooldown(this, 30);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.archaion.echo_mace.desc",
                Component.translatable("item.archaion.echo_mace.keyword.echo_mace").withStyle(ChatFormatting.AQUA))
                .withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(CommonComponents.EMPTY);
    }
}
