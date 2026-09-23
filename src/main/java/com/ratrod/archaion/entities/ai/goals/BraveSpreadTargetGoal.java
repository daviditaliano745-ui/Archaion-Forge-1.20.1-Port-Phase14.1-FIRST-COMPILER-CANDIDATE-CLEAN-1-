package com.ratrod.archaion.entities.ai.goals;

import com.ratrod.archaion.entities.Brave;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Recreates Brave's target spreading: normal Braves choose a nearby player; charged Braves
 * owned by the same entity distribute themselves over the available nearby players.
 */
public class BraveSpreadTargetGoal extends TargetGoal {
    private final Brave brave;
    private int refreshTicks;

    public BraveSpreadTargetGoal(Brave brave) {
        super(brave, false);
        this.brave = brave;
    }

    @Override public boolean canUse() { return true; }
    @Override public boolean canContinueToUse() { return true; }

    @Override
    public void tick() {
        if (brave.level().isClientSide || --refreshTicks > 0) return;
        refreshTicks = 10;

        LivingEntity current = brave.getTarget();
        if (current != null && !isValid(current)) brave.setTarget(null);

        if (brave.isCharged() && brave.getOwnerUUID() != null && brave.level() instanceof ServerLevel) {
            spreadChargedTargets();
        } else {
            chooseNearestPlayer();
        }
    }

    private void chooseNearestPlayer() {
        AABB area = brave.getBoundingBox().inflate(48.0D);
        Player nearest = brave.level().getEntitiesOfClass(Player.class, area, this::isValid)
                .stream().min(Comparator.comparingDouble(brave::distanceToSqr)).orElse(null);
        brave.setTarget(nearest);
    }

    private void spreadChargedTargets() {
        LivingEntity owner = brave.getOwner();
        if (owner == null || !owner.isAlive()) {
            chooseNearestPlayer();
            return;
        }

        List<Player> players = brave.level().getEntitiesOfClass(Player.class,
                brave.getBoundingBox().inflate(48.0D), this::isValid);
        players.sort(Comparator.comparingInt(Player::getId));
        if (players.isEmpty()) {
            brave.setTarget(null);
            return;
        }

        UUID ownerId = brave.getOwnerUUID();
        List<Brave> siblings = brave.level().getEntitiesOfClass(Brave.class,
                owner.getBoundingBox().inflate(512.0D), candidate ->
                        candidate.isAlive() && candidate.isCharged() && ownerId.equals(candidate.getOwnerUUID()));
        siblings.sort(Comparator.comparingInt(Brave::getId));

        int index = siblings.indexOf(brave);
        if (index < 0) index = 0;
        brave.setTarget(players.get(index % players.size()));
    }

    private boolean isValid(LivingEntity target) {
        if (!target.isAlive() || target.isSpectator() || target.isInvulnerable()) return false;
        if (target instanceof Player player && player.isCreative()) return false;
        return brave.canAttack(target);
    }
}
