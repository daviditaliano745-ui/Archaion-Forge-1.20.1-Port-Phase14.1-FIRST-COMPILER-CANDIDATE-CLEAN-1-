package com.ratrod.archaion.api.trial;

import com.ratrod.archaion.registry.ACBlockEntities;
import com.ratrod.archaion.registry.ACItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * 1.20.1 implementation of Archaion's deepslate vault.
 * Uses the original Echo Key, 4.0/4.5 eligibility ranges and five-roll reward pool.
 */
public class TrialVaultBlockEntity extends BlockEntity {
    private static final double ACTIVATION_RANGE = 4.0D;
    private static final double DEACTIVATION_RANGE = 4.5D;
    private final Set<UUID> rewardedPlayers = new HashSet<>();
    private UUID openingPlayer;
    private int stateTimer;
    public int clientTicks;

    public TrialVaultBlockEntity(BlockPos pos, BlockState state) { super(ACBlockEntities.TRIAL_VAULT.get(), pos, state); }
    public static void clientTick(Level level, BlockPos pos, BlockState state, TrialVaultBlockEntity be) { be.clientTicks++; }
    public static void serverTick(Level level, BlockPos pos, BlockState state, TrialVaultBlockEntity be) {
        if (level instanceof ServerLevel server) be.tickServer(server);
    }

    private void tickServer(ServerLevel level) {
        VaultState state = state();
        if (state == VaultState.UNLOCKING) {
            if (++stateTimer >= 20) { stateTimer = 0; setState(VaultState.EJECTING); }
            return;
        }
        if (state == VaultState.EJECTING) {
            if (++stateTimer == 5) ejectRewards(level);
            if (stateTimer >= 30) { stateTimer = 0; openingPlayer = null; updateEligibilityState(level); }
            return;
        }
        updateEligibilityState(level);
    }

    private void updateEligibilityState(ServerLevel level) {
        boolean eligible = !level.getEntitiesOfClass(ServerPlayer.class, new AABB(worldPosition).inflate(ACTIVATION_RANGE), this::eligible).isEmpty();
        if (eligible) setState(VaultState.ACTIVE);
        else if (state() == VaultState.ACTIVE) {
            boolean stillNear = !level.getEntitiesOfClass(ServerPlayer.class, new AABB(worldPosition).inflate(DEACTIVATION_RANGE), this::eligible).isEmpty();
            if (!stillNear) setState(VaultState.INACTIVE);
        } else if (state() != VaultState.INACTIVE) setState(VaultState.INACTIVE);
    }

    private boolean eligible(ServerPlayer player) {
        return player.isAlive() && !player.isSpectator() && !rewardedPlayers.contains(player.getUUID()) && hasKey(player);
    }
    private boolean hasKey(Player player) {
        return player.getMainHandItem().is(ACItems.ECHO_KEY.get()) || player.getOffhandItem().is(ACItems.ECHO_KEY.get());
    }

    public boolean tryUnlock(Player player, InteractionHand hand) {
        if (!(level instanceof ServerLevel server) || rewardedPlayers.contains(player.getUUID())) return false;
        if (!player.getItemInHand(hand).is(ACItems.ECHO_KEY.get())) return false;
        if (state() == VaultState.UNLOCKING || state() == VaultState.EJECTING) return false;
        if (!player.getAbilities().instabuild) player.getItemInHand(hand).shrink(1);
        openingPlayer = player.getUUID();
        rewardedPlayers.add(player.getUUID());
        stateTimer = 0;
        setState(VaultState.UNLOCKING);
        server.playSound(null, worldPosition, SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.BLOCKS, 1.0F, 1.35F);
        markUpdated();
        return true;
    }

    private void ejectRewards(ServerLevel level) {
        Direction facing = getBlockState().getValue(TrialVaultBlock.FACING);
        for (int i = 0; i < 5; i++) {
            ItemStack reward = randomVaultReward(level.random);
            double x = worldPosition.getX() + .5D + facing.getStepX() * .7D;
            double y = worldPosition.getY() + .8D;
            double z = worldPosition.getZ() + .5D + facing.getStepZ() * .7D;
            ItemEntity item = new ItemEntity(level, x, y, z, reward);
            item.setDeltaMovement(facing.getStepX() * .18D, .22D + i * .015D, facing.getStepZ() * .18D);
            level.addFreshEntity(item);
        }
        level.playSound(null, worldPosition, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1.2F, 0.65F);
    }

    /** Mirrors the original deepslate_vault loot weights. Density is included automatically if a 1.20.1 backport registers it. */
    private ItemStack randomVaultReward(RandomSource random) {
        // Effective weights from original table: enchanted apple3, echo charge5, golden apple5, diamond4,
        // golden carrot5, gold ingot5, gold block2, diamond block1, grace template1, xp bottle3,
        // density1, mending2, unbreaking2, sharpness1, power1.
        Enchantment density = optionalEnchantment("minecraft:density", "vanillabackport:density");
        int roll = random.nextInt(density == null ? 40 : 41);
        if (roll < 3) return new ItemStack(Items.ENCHANTED_GOLDEN_APPLE);
        if (roll < 8) return new ItemStack(ACItems.ECHO_CHARGE.get());
        if (roll < 13) return new ItemStack(Items.GOLDEN_APPLE, 2 + random.nextInt(3));
        if (roll < 17) return new ItemStack(Items.DIAMOND, 4 + random.nextInt(5));
        if (roll < 22) return new ItemStack(Items.GOLDEN_CARROT, 6 + random.nextInt(3));
        if (roll < 27) return new ItemStack(Items.GOLD_INGOT, 8 + random.nextInt(9));
        if (roll < 29) return new ItemStack(Items.GOLD_BLOCK, 1 + random.nextInt(2));
        if (roll == 29) return new ItemStack(Items.DIAMOND_BLOCK, 1 + random.nextInt(2));
        if (roll == 30) return new ItemStack(ACItems.ECHOS_GRACE_UPGRADE_SMITHING_TEMPLATE.get());
        if (roll < 34) return new ItemStack(Items.EXPERIENCE_BOTTLE, 4 + random.nextInt(9));
        if (density != null) {
            if (roll == 34) return EnchantedBookItem.createForEnchantment(new EnchantmentInstance(density, 1 + random.nextInt(5)));
            roll--; // Shift the remaining original weights back into the 40-entry layout.
        }
        if (roll < 36) return EnchantedBookItem.createForEnchantment(new EnchantmentInstance(Enchantments.MENDING, 1));
        if (roll < 38) return EnchantedBookItem.createForEnchantment(new EnchantmentInstance(Enchantments.UNBREAKING, 1 + random.nextInt(3)));
        if (roll == 38) return EnchantedBookItem.createForEnchantment(new EnchantmentInstance(Enchantments.SHARPNESS, 1 + random.nextInt(5)));
        return EnchantedBookItem.createForEnchantment(new EnchantmentInstance(Enchantments.POWER_ARROWS, 1 + random.nextInt(5)));
    }

    @Nullable
    private static Enchantment optionalEnchantment(String... ids) {
        for (String id : ids) {
            ResourceLocation key = ResourceLocation.tryParse(id);
            if (key == null) continue;
            Enchantment enchantment = ForgeRegistries.ENCHANTMENTS.getValue(key);
            if (enchantment != null) return enchantment;
        }
        return null;
    }

    private VaultState state() {
        BlockState state = getBlockState();
        return state.hasProperty(TrialVaultBlock.STATE) ? state.getValue(TrialVaultBlock.STATE) : VaultState.INACTIVE;
    }
    private void setState(VaultState state) {
        if (level != null && getBlockState().hasProperty(TrialVaultBlock.STATE) && getBlockState().getValue(TrialVaultBlock.STATE) != state)
            level.setBlock(worldPosition, getBlockState().setValue(TrialVaultBlock.STATE, state), 3);
        markUpdated();
    }

    @Override public void load(CompoundTag tag) {
        super.load(tag);
        rewardedPlayers.clear();
        long[] players = tag.getLongArray("rewarded_players");
        for (int i = 0; i + 1 < players.length; i += 2) rewardedPlayers.add(new UUID(players[i], players[i + 1]));
        if (tag.hasUUID("opening_player")) openingPlayer = tag.getUUID("opening_player");
        stateTimer = tag.getInt("state_timer");
        // Untouched 1.21 structure vault NBT intentionally needs no conversion: key/loot config is intrinsic to this variant.
    }
    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        long[] ids = new long[rewardedPlayers.size() * 2]; int i = 0;
        for (UUID id : rewardedPlayers) { ids[i++] = id.getMostSignificantBits(); ids[i++] = id.getLeastSignificantBits(); }
        tag.putLongArray("rewarded_players", ids);
        if (openingPlayer != null) tag.putUUID("opening_player", openingPlayer);
        tag.putInt("state_timer", stateTimer);
    }
    private void markUpdated() {
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }
    @Nullable @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public CompoundTag getUpdateTag() { return saveWithoutMetadata(); }
}
