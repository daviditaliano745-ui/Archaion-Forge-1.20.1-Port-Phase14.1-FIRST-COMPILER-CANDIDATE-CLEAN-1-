package com.ratrod.archaion.item;

import com.ratrod.archaion.entities.projectile.EchoStarProjectile;
import com.ratrod.archaion.registry.ACSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/** Forge 1.20.1 port of Archaion's throwable Echo Charge item. */
public class EchoChargeItem extends Item {
    public EchoChargeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.getCooldowns().addCooldown(this, 4);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), ACSounds.LOD_SHOOT.get(),
                SoundSource.NEUTRAL, 0.5F, 1.6F + level.getRandom().nextFloat() * 0.2F);

        if (level instanceof ServerLevel server) {
            EchoStarProjectile projectile = new EchoStarProjectile(server, player);
            projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
            server.addFreshEntity(projectile);
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(
                        "item.archaion.echo_charge.desc",
                        Component.translatable("item.archaion.echo_charge.keyword.slumbering_titan").withStyle(ChatFormatting.AQUA),
                        Component.translatable("item.archaion.echo_charge.keyword.special_weapon").withStyle(ChatFormatting.YELLOW))
                .withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(CommonComponents.EMPTY);
    }
}
