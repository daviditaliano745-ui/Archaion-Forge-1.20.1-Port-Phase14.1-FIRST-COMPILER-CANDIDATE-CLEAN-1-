package com.ratrod.archaion.entities.ai.systems;

import com.ratrod.archaion.entities.Archaic;
import com.ratrod.archaion.entities.LastOfDeepslate;
import com.ratrod.archaion.network.BossBarDataOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

/** Server-side raid/protection state retained from the 1.21 implementation. */
public final class ArchaicRaid {
    private final LastOfDeepslate entity;
    private int phasesTriggered;
    private int archaicsIntended;
    private int lastRaidAlive = -1;
    private int lastRaidTotal = -1;
    private int lastPhase = -1;

    public ArchaicRaid(LastOfDeepslate entity) { this.entity = entity; }

    public int getPhasesTriggered() { return phasesTriggered; }
    public void updatePhase() { entity.setPhase(++phasesTriggered); }
    public int getArchaicsIntended() { return archaicsIntended; }
    public void setArchaicsIntended(int intended) { archaicsIntended = Math.max(0, intended); }

    public int countChargedArchaics() {
        if (entity.level().isClientSide) return 0;
        UUID bossId = entity.getUUID();
        return entity.level().getEntitiesOfClass(Monster.class, entity.getBoundingBox().inflate(80.0D), mob -> {
            if (!(mob instanceof Archaic archaic) || !archaic.isCharged()) return false;
            return bossId.equals(archaic.getOwnerUUID());
        }).size();
    }

    public int countNearbyPlayers() {
        if (entity.level().isClientSide) return 0;
        return entity.level().getEntitiesOfClass(Player.class, entity.getBoundingBox().inflate(80.0D),
                EntitySelector.NO_CREATIVE_OR_SPECTATOR).size();
    }

    public boolean isOwnedArchaic(LivingEntity living) {
        return living instanceof Archaic archaic && entity.getUUID().equals(archaic.getOwnerUUID());
    }

    public float getArchaicProtectionMultiplier(int chargedAlive) {
        if (archaicsIntended <= 0 || chargedAlive <= 0) return 1.0F;
        float ratio = Mth.clamp((float) chargedAlive / (float) archaicsIntended, 0.0F, 1.0F);
        float protection = 0.30F + (0.95F - 0.30F) * ratio;
        return 1.0F - protection;
    }

    public void tick() {
        int alive = countChargedArchaics();
        int total = archaicsIntended;
        int phase = entity.getPhase();
        if (alive != lastRaidAlive || total != lastRaidTotal || phase != lastPhase) {
            lastRaidAlive = alive;
            lastRaidTotal = total;
            lastPhase = phase;
            entity.setHasChargedArchaics(alive > 0);
            entity.syncBossBarData(alive);
        }
    }

    public void writeBossBarData(BossBarDataOutput output, int chargedAlive) {
        output.add("hasChargedArchaics", chargedAlive > 0 ? 1 : 0)
                .add("archaicRaidAlive", Math.max(0, chargedAlive))
                .add("archaicRaidTotal", archaicsIntended)
                .add("archaicPhase", entity.getPhase());
    }

    public void writeBossBarData(BossBarDataOutput output) {
        writeBossBarData(output, countChargedArchaics());
    }

    public void load(CompoundTag tag) {
        phasesTriggered = tag.getInt("hurlArchaicsCount");
        archaicsIntended = tag.getInt("hurlArchaicsIntended");
        entity.setPhase(phasesTriggered);
    }

    public void save(CompoundTag tag) {
        tag.putInt("hurlArchaicsCount", phasesTriggered);
        tag.putInt("hurlArchaicsIntended", archaicsIntended);
    }
}
