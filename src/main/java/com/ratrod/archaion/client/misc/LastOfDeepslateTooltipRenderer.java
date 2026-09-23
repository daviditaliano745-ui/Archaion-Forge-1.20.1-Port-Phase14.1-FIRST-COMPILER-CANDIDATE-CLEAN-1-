package com.ratrod.archaion.client.misc;

import com.ratrod.archaion.entities.LastOfDeepslate;
import com.ratrod.archaion.entities.SleepingState;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

/** Sleeping-boss look tooltip, preserving the original 32-block LOS raycast and bobbing presentation. */
public final class LastOfDeepslateTooltipRenderer {
    private static final double RANGE = 32.0D;

    public static void render(GuiGraphics graphics, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) return;

        LastOfDeepslate boss = findLookedAtLastOfDeepslate(minecraft.player, partialTick);
        if (boss == null || boss.getSleepingState() != SleepingState.SLEEPING) return;

        renderTooltip(graphics, boss, minecraft, boss.getDisplayName(), partialTick);
    }

    private static void renderTooltip(GuiGraphics graphics, LastOfDeepslate boss,
                                      Minecraft minecraft, Component bossName, float partialTick) {
        Font font = minecraft.font;
        float time = minecraft.player.tickCount + partialTick;
        Component title = bossName.copy().withStyle(ChatFormatting.AQUA);
        int centerX = graphics.guiWidth() / 2;
        int centerY = graphics.guiHeight() / 2;

        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, (float) Math.sin(time * 0.1F) * 2.0F, 0.0F);
        graphics.renderTooltip(font,
                List.of(title),
                Optional.of(new EchoChargeRequiredTooltip(Math.max(0, 4 - boss.getEchoChargesFed()))),
                centerX,
                centerY);
        graphics.pose().popPose();
    }

    private static LastOfDeepslate findLookedAtLastOfDeepslate(Entity viewer, float partialTick) {
        Vec3 eye = viewer.getEyePosition(partialTick);
        Vec3 look = viewer.getViewVector(partialTick);
        Vec3 end = eye.add(look.scale(RANGE));

        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                viewer,
                eye,
                end,
                viewer.getBoundingBox().expandTowards(look.scale(RANGE)).inflate(1.0D),
                target -> target instanceof LastOfDeepslate && target.isAlive(),
                RANGE * RANGE);
        if (entityHit == null || !(entityHit.getEntity() instanceof LastOfDeepslate boss)) return null;

        HitResult blockHit = viewer.level().clip(new ClipContext(
                eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, viewer));
        double entityDistance = eye.distanceToSqr(entityHit.getLocation());
        if (blockHit.getType() != HitResult.Type.MISS
                && eye.distanceToSqr(blockHit.getLocation()) < entityDistance) {
            return null;
        }
        return boss;
    }

    private LastOfDeepslateTooltipRenderer() { }
}
