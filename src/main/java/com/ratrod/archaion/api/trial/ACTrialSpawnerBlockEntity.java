package com.ratrod.archaion.api.trial;

import com.ratrod.archaion.registry.ACBlockEntities;
import com.ratrod.archaion.registry.ACItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Backport of the 1.21 TrialSpawner runtime used by Archaion.
 *
 * Original Archaion config preserved here:
 * spawnRange=4, totalMobs=6, simultaneousMobs=2,
 * totalMobsAddedPerPlayer=1, simultaneousAddedPerPlayer=0.5,
 * ticksBetweenSpawn=100, requiredPlayerRange=14, cooldown=36000.
 */
public class ACTrialSpawnerBlockEntity extends BlockEntity {
    public static final int SPAWN_RANGE = 4;
    public static final float BASE_TOTAL_MOBS = 6.0F;
    public static final float BASE_SIMULTANEOUS_MOBS = 2.0F;
    public static final float TOTAL_ADDED_PER_PLAYER = 1.0F;
    public static final float SIMULTANEOUS_ADDED_PER_PLAYER = 0.5F;
    public static final int TICKS_BETWEEN_SPAWN = 100;
    public static final int REQUIRED_PLAYER_RANGE = 14;
    public static final int COOLDOWN_TICKS = 36000;

    private ResourceLocation entityId;
    private String equipmentProfile = "";
    private final Set<UUID> trackedMobs = new HashSet<>();
    private final Set<UUID> trialPlayers = new HashSet<>();
    private int spawnedTotal;
    private int spawnDelay;
    private int stateTimer;
    private int cooldown;
    public int clientTicks;
    private double clientSpin;
    private double clientOldSpin;
    @Nullable private Entity clientDisplayEntity;
    @Nullable private ResourceLocation clientDisplayEntityId;

    public ACTrialSpawnerBlockEntity(BlockPos pos, BlockState state) {
        super(ACBlockEntities.TRIAL_SPAWNER.get(), pos, state);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, ACTrialSpawnerBlockEntity be) {
        be.clientTicks++;
        be.clientOldSpin = be.clientSpin;
        float delay = Math.max(0, be.spawnDelay);
        be.clientSpin = (be.clientSpin + 1000.0D / (delay + 200.0D)) % 360.0D;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ACTrialSpawnerBlockEntity be) {
        if (!(level instanceof ServerLevel server)) return;
        be.tickServer(server);
    }

    private void tickServer(ServerLevel level) {
        TrialSpawnerState state = state();
        switch (state) {
            case INACTIVE -> {
                if (entityId != null) setState(TrialSpawnerState.WAITING_FOR_PLAYERS);
            }
            case WAITING_FOR_PLAYERS -> waitForPlayers(level);
            case ACTIVE -> tickActive(level);
            case WAITING_FOR_REWARD_EJECTION -> {
                if (++stateTimer >= 40) { stateTimer = 0; setState(TrialSpawnerState.EJECTING_REWARD); }
            }
            case EJECTING_REWARD -> ejectRewards(level);
            case COOLDOWN -> {
                if (cooldown > 0) cooldown--;
                if (cooldown <= 0) resetTrial();
            }
        }
    }

    private void waitForPlayers(ServerLevel level) {
        if (entityId == null) return;
        List<ServerPlayer> nearby = eligiblePlayers(level, REQUIRED_PLAYER_RANGE);
        if (nearby.isEmpty()) return;
        trialPlayers.clear();
        for (ServerPlayer player : nearby) trialPlayers.add(player.getUUID());
        spawnedTotal = 0;
        trackedMobs.clear();
        spawnDelay = 0;
        setState(TrialSpawnerState.ACTIVE);
        level.playSound(null, worldPosition, SoundEvents.SCULK_CATALYST_BLOOM, SoundSource.BLOCKS, 1.0F, 1.2F);
    }

    private void tickActive(ServerLevel level) {
        cleanTrackedMobs(level);
        // Vanilla trial spawners add newly-arriving players while the trial is active.
        for (ServerPlayer player : eligiblePlayers(level, REQUIRED_PLAYER_RANGE)) trialPlayers.add(player.getUUID());

        int players = Math.max(1, trialPlayers.size());
        int totalTarget = Mth.ceil(BASE_TOTAL_MOBS + (players - 1) * TOTAL_ADDED_PER_PLAYER);
        int simultaneousTarget = Mth.ceil(BASE_SIMULTANEOUS_MOBS + (players - 1) * SIMULTANEOUS_ADDED_PER_PLAYER);

        if (spawnedTotal >= totalTarget) {
            if (trackedMobs.isEmpty()) {
                stateTimer = 0;
                setState(TrialSpawnerState.WAITING_FOR_REWARD_EJECTION);
            }
            return;
        }

        if (trackedMobs.size() >= simultaneousTarget) return;
        if (spawnDelay-- > 0) return;
        if (spawnOne(level)) {
            spawnedTotal++;
            spawnDelay = TICKS_BETWEEN_SPAWN;
        } else {
            spawnDelay = 20;
        }
    }

    private boolean spawnOne(ServerLevel level) {
        if (entityId == null) return false;
        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(entityId);
        if (type == null) return false;

        RandomSource random = level.random;
        for (int attempt = 0; attempt < 12; attempt++) {
            int dx = random.nextInt(SPAWN_RANGE * 2 + 1) - SPAWN_RANGE;
            int dz = random.nextInt(SPAWN_RANGE * 2 + 1) - SPAWN_RANGE;
            int dy = random.nextInt(3) - 1;
            BlockPos spawnPos = worldPosition.offset(dx, dy + 1, dz);
            if (!level.getBlockState(spawnPos).getCollisionShape(level, spawnPos).isEmpty()) continue;
            if (!level.getBlockState(spawnPos.below()).isFaceSturdy(level, spawnPos.below(), Direction.UP)) continue;

            Entity created = type.create(level);
            if (created == null) return false;
            created.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, random.nextFloat() * 360F, 0F);
            if (created instanceof Mob mob) {
                mob.finalizeSpawn(level, level.getCurrentDifficultyAt(spawnPos), MobSpawnType.TRIGGERED, null, null);
                applyEquipment(mob, random);
            }
            created.getPersistentData().putLong("ArchaionTrialSpawnerPos", worldPosition.asLong());
            if (!level.addFreshEntity(created)) return false;
            trackedMobs.add(created.getUUID());
            level.playSound(null, spawnPos, SoundEvents.SCULK_SHRIEKER_SHRIEK, SoundSource.BLOCKS, 0.55F, 1.45F);
            markUpdated();
            return true;
        }
        return false;
    }

    private void applyEquipment(Mob mob, RandomSource random) {
        // 1.21 equipment loot tables contain 75%-chance enchanted diamond armour plus a weapon.
        // This 1.20.1 runtime mirrors the gameplay loadout; trim cosmetics are deferred to the visual/data pass.
        if (equipmentProfile.endsWith("deepslate_spawner_melee")) {
            mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(random.nextBoolean() ? Items.DIAMOND_SWORD : Items.DIAMOND_AXE));
        } else if (equipmentProfile.endsWith("deepslate_spawner_ranged")) {
            mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
        } else return;
        if (random.nextFloat() < 0.75F) mob.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.DIAMOND_HELMET));
        if (random.nextFloat() < 0.75F) mob.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.DIAMOND_CHESTPLATE));
        if (random.nextFloat() < 0.75F) mob.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.DIAMOND_BOOTS));
        for (EquipmentSlot slot : EquipmentSlot.values()) mob.setDropChance(slot, 0.0F);
    }

    private void cleanTrackedMobs(ServerLevel level) {
        trackedMobs.removeIf(uuid -> {
            Entity entity = level.getEntity(uuid);
            return entity == null || !entity.isAlive() || entity.distanceToSqr(worldPosition.getX() + .5, worldPosition.getY() + .5, worldPosition.getZ() + .5) > 64.0D * 64.0D;
        });
    }

    private void ejectRewards(ServerLevel level) {
        if (trialPlayers.isEmpty()) trialPlayers.addAll(eligiblePlayerIds(level));
        // Normal Archaion config: weighted reward-table choice = misc(3), echo-key(2).
        int count = Math.max(1, trialPlayers.size());
        for (int i = 0; i < count; i++) eject(level, randomSpawnerReward(level.random));
        cooldown = COOLDOWN_TICKS;
        setState(TrialSpawnerState.COOLDOWN);
        level.playSound(null, worldPosition, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1.2F, 0.7F);
    }

    private ItemStack randomSpawnerReward(RandomSource random) {
        if (random.nextInt(5) >= 3) return new ItemStack(ACItems.ECHO_KEY.get());
        int pick = random.nextInt(11); // misc weights: 4,3,1,1,1,1
        if (pick < 4) return new ItemStack(Items.GOLDEN_CARROT, 3 + random.nextInt(4));
        if (pick < 7) return new ItemStack(Items.GOLDEN_APPLE, 2 + random.nextInt(3));
        if (pick == 7) return new ItemStack(Items.ENCHANTED_GOLDEN_APPLE);
        ItemStack potion = new ItemStack(pick == 10 ? Items.SPLASH_POTION : Items.POTION);
        if (pick == 8) PotionUtils.setPotion(potion, Potions.TURTLE_MASTER);
        else if (pick == 9) PotionUtils.setPotion(potion, Potions.LONG_REGENERATION);
        else PotionUtils.setPotion(potion, Potions.STRONG_HEALING);
        return potion;
    }

    private void eject(ServerLevel level, ItemStack stack) {
        ItemEntity item = new ItemEntity(level, worldPosition.getX() + .5D, worldPosition.getY() + 1.2D, worldPosition.getZ() + .5D, stack);
        item.setDeltaMovement((level.random.nextDouble() - .5D) * .15D, .35D, (level.random.nextDouble() - .5D) * .15D);
        level.addFreshEntity(item);
    }

    private List<ServerPlayer> eligiblePlayers(ServerLevel level, double range) {
        AABB box = new AABB(worldPosition).inflate(range);
        return level.getEntitiesOfClass(ServerPlayer.class, box, p -> !p.isCreative() && !p.isSpectator() && p.isAlive());
    }
    private Set<UUID> eligiblePlayerIds(ServerLevel level) {
        Set<UUID> ids = new HashSet<>();
        for (ServerPlayer p : eligiblePlayers(level, REQUIRED_PLAYER_RANGE)) ids.add(p.getUUID());
        return ids;
    }

    private void resetTrial() {
        cooldown = 0; spawnedTotal = 0; spawnDelay = 0; stateTimer = 0;
        trackedMobs.clear(); trialPlayers.clear();
        setState(entityId == null ? TrialSpawnerState.INACTIVE : TrialSpawnerState.WAITING_FOR_PLAYERS);
    }

    public TrialSpawnerState state() {
        BlockState state = getBlockState();
        return state.hasProperty(ACTrialSpawnerBlock.STATE) ? state.getValue(ACTrialSpawnerBlock.STATE) : TrialSpawnerState.INACTIVE;
    }
    public void setState(TrialSpawnerState state) {
        if (level != null && getBlockState().hasProperty(ACTrialSpawnerBlock.STATE) && getBlockState().getValue(ACTrialSpawnerBlock.STATE) != state) {
            level.setBlock(worldPosition, getBlockState().setValue(ACTrialSpawnerBlock.STATE, state), 3);
        }
        markUpdated();
    }
    public void setEntityId(EntityType<?> type, RandomSource random) {
        ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey(type);
        if (key != null) {
            entityId = key;
            clientDisplayEntity = null;
            clientDisplayEntityId = null;
            markUpdated();
        }
    }

    public double getClientSpin() { return clientSpin; }
    public double getClientOldSpin() { return clientOldSpin; }

    @Nullable
    public Entity getOrCreateClientDisplayEntity() {
        if (level == null || entityId == null) return null;
        if (clientDisplayEntity == null || !entityId.equals(clientDisplayEntityId)) {
            EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(entityId);
            if (type == null) return null;
            clientDisplayEntity = type.create(level);
            clientDisplayEntityId = entityId;
        }
        return clientDisplayEntity;
    }

    @Override public void load(CompoundTag tag) {
        super.load(tag);
        // Understand both our compact 1.20 save and the untouched 1.21 structure-template NBT.
        if (tag.contains("entity_id")) entityId = ResourceLocation.tryParse(tag.getString("entity_id"));
        if (tag.contains("spawn_data", 10)) {
            CompoundTag spawnData = tag.getCompound("spawn_data");
            if (spawnData.contains("entity", 10)) {
                String id = spawnData.getCompound("entity").getString("id");
                if (!id.isEmpty()) entityId = ResourceLocation.tryParse(id);
            }
            if (spawnData.contains("equipment", 10)) equipmentProfile = spawnData.getCompound("equipment").getString("loot_table");
        }
        if (tag.contains("equipment_profile")) equipmentProfile = tag.getString("equipment_profile");
        clientDisplayEntity = null;
        clientDisplayEntityId = null;
        spawnedTotal = tag.getInt("spawned_total");
        spawnDelay = tag.getInt("spawn_delay");
        stateTimer = tag.getInt("state_timer");
        cooldown = tag.getInt("cooldown");
        trackedMobs.clear();
        long[] mobs = tag.getLongArray("tracked_mobs");
        for (int i = 0; i + 1 < mobs.length; i += 2) trackedMobs.add(new UUID(mobs[i], mobs[i + 1]));
        trialPlayers.clear();
        long[] players = tag.getLongArray("trial_players");
        for (int i = 0; i + 1 < players.length; i += 2) trialPlayers.add(new UUID(players[i], players[i + 1]));
    }

    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (entityId != null) tag.putString("entity_id", entityId.toString());
        tag.putString("equipment_profile", equipmentProfile);
        tag.putInt("spawned_total", spawnedTotal);
        tag.putInt("spawn_delay", spawnDelay);
        tag.putInt("state_timer", stateTimer);
        tag.putInt("cooldown", cooldown);
        tag.putLongArray("tracked_mobs", uuidArray(trackedMobs));
        tag.putLongArray("trial_players", uuidArray(trialPlayers));
    }

    private static long[] uuidArray(Collection<UUID> ids) {
        long[] out = new long[ids.size() * 2]; int i = 0;
        for (UUID id : ids) { out[i++] = id.getMostSignificantBits(); out[i++] = id.getLeastSignificantBits(); }
        return out;
    }

    private void markUpdated() {
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    @Nullable @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public CompoundTag getUpdateTag() { return saveWithoutMetadata(); }
}
