package com.ratrod.archaion.entities.ai.goals;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Direct 1.20.1 backport of the original Last of Deepslate target cycler.
 * It intentionally considers both survival-mode players and iron golems in a
 * 64-block box, then periodically chooses one of them at random.
 */
public class LODAttackableRandomTargetGoal extends TargetGoal {
    private int cycleDelay;

    public LODAttackableRandomTargetGoal(Mob mob) {
        super(mob, true);
    }

    @Override
    public boolean canUse() {
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return true;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        cycleDelay = 100 + mob.getRandom().nextInt(40);
    }

    @Override
    public void tick() {
        if (mob.level().isClientSide) {
            return;
        }

        LivingEntity current = mob.getTarget();
        if (current != null && !isValidTarget(current)) {
            mob.setTarget(null);
        }

        if (mob.getTarget() == null || cycleDelay-- <= 0) {
            setTarget();
            cycleDelay = 100 + mob.getRandom().nextInt(40);
        }
    }

    private boolean isValidTarget(LivingEntity target) {
        if (!target.isAlive() || target.isSpectator()) {
            return false;
        }
        return !(target instanceof Player player) || !player.isCreative();
    }

    private void setTarget() {
        List<LivingEntity> candidates = nearbyTargets();
        if (candidates.isEmpty()) {
            mob.setTarget(null);
        } else {
            mob.setTarget(candidates.get(mob.getRandom().nextInt(candidates.size())));
        }
    }

    private List<LivingEntity> nearbyTargets() {
        List<LivingEntity> result = new ArrayList<>();
        AABB range = mob.getBoundingBox().inflate(64.0D);
        Predicate<LivingEntity> predicate = this::isValidTarget;
        result.addAll(mob.level().getEntitiesOfClass(Player.class, range, predicate));
        result.addAll(mob.level().getEntitiesOfClass(IronGolem.class, range, predicate));
        return result;
    }
}
