package com.ratrod.archaion.client.misc;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Client-only Ancient Keep ambience state mirrored from the original implementation. */
public final class AncientKeepClientData {
    public static AABB ANCIENT_KEEP_BOX;
    private static float ancientKeepFogMix;
    private static final float ANCIENT_KEEP_FOG_RAMP = 0.05F;

    public static void tick(Player player) {
        AABB box = ANCIENT_KEEP_BOX;
        boolean inside = box != null && box.contains(player.position());
        if (inside && ancientKeepFogMix < 1.0F) {
            ancientKeepFogMix = Math.min(1.0F, ancientKeepFogMix + ANCIENT_KEEP_FOG_RAMP);
        } else if (!inside && ancientKeepFogMix > 0.0F) {
            ancientKeepFogMix = Math.max(0.0F, ancientKeepFogMix - ANCIENT_KEEP_FOG_RAMP);
        }

        if (inside) {
            spawnAmbientParticles(player.level(), box, player.position(), player.getRandom());
        }
    }

    private static void spawnAmbientParticles(Level level, AABB box, Vec3 playerPos, RandomSource random) {
        for (int i = 0; i < 12; i++) {
            Vec3 point = randomPointNearPlayer(playerPos, box, random);
            level.addParticle(ParticleTypes.SCULK_SOUL,
                    point.x, point.y, point.z,
                    0.0D, 0.2D + random.nextFloat() * 0.3D, 0.0D);
        }
    }

    /** The original passes the structure box here but intentionally samples a 32-block cube around the player. */
    private static Vec3 randomPointNearPlayer(Vec3 playerPos, AABB ignoredBox, RandomSource random) {
        double radius = 32.0D;
        return new Vec3(
                playerPos.x + (random.nextDouble() * 2.0D - 1.0D) * radius,
                playerPos.y + (random.nextDouble() * 2.0D - 1.0D) * radius,
                playerPos.z + (random.nextDouble() * 2.0D - 1.0D) * radius);
    }

    public static float keepFogFactor() {
        return (float) Mth.smoothstep(ancientKeepFogMix);
    }

    private AncientKeepClientData() { }
}
