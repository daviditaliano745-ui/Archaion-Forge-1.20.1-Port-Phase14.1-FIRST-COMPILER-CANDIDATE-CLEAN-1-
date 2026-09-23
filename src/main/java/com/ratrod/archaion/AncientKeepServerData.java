package com.ratrod.archaion;

import com.ratrod.archaion.network.ACNetwork;
import com.ratrod.archaion.network.s2c.AncientKeepAmbientPacket;
import com.ratrod.archaion.registry.ACStructureTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.phys.AABB;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Lightweight server-side Ancient Keep presence tracker.
 *
 * The structure lookup only runs once every ten player ticks (see ACServerEvents), and the
 * bounding box packet is sent only on the transition from outside -> inside, matching the
 * original 1.21 implementation without adding any per-tick packet traffic.
 */
public final class AncientKeepServerData {
    private static final Set<UUID> INSIDE_ANCIENT_KEEP = new HashSet<>();

    public static void tickPlayer(ServerPlayer player) {
        UUID id = player.getUUID();
        if (isInsideAncientKeep(player)) {
            if (INSIDE_ANCIENT_KEEP.add(id)) sendAncientKeepBox(player);
        } else {
            INSIDE_ANCIENT_KEEP.remove(id);
        }
    }

    public static void onPlayerLoggedOut(Player player) {
        INSIDE_ANCIENT_KEEP.remove(player.getUUID());
    }

    private static boolean isInsideAncientKeep(ServerPlayer player) {
        ServerLevel level = (ServerLevel) player.level();
        StructureStart start = level.structureManager().getStructureWithPieceAt(
                player.blockPosition(), ACStructureTags.ON_ANCIENT_KEEP_MAPS);
        return start.isValid();
    }

    private static void sendAncientKeepBox(ServerPlayer player) {
        ServerLevel level = (ServerLevel) player.level();
        StructureStart start = level.structureManager().getStructureWithPieceAt(
                player.blockPosition(), ACStructureTags.ON_ANCIENT_KEEP_MAPS);
        if (!start.isValid()) return;

        BoundingBox box = start.getBoundingBox();
        ACNetwork.sendToPlayer(player, new AncientKeepAmbientPacket(new AABB(
                box.minX(), box.minY(), box.minZ(),
                box.maxX(), box.maxY(), box.maxZ())));
    }

    private AncientKeepServerData() { }
}
