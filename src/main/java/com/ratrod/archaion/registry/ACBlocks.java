package com.ratrod.archaion.registry;

import com.ratrod.archaion.Archaion;
import com.ratrod.archaion.block.ReinforcedBarBlock;
import com.ratrod.archaion.block.HologramBlock;
import com.ratrod.archaion.block.TeleporterBlock;
import com.ratrod.archaion.block.WaterloggedGrateBlock;
import com.ratrod.archaion.api.trial.ACTrialSpawnerBlock;
import com.ratrod.archaion.api.trial.TrialVaultBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

/** Phase-1 Forge rewrite of the original NeoForge block registry. */
public final class ACBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Archaion.MODID);

    private static BlockBehaviour.Properties reinforced() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE)
                .instrument(NoteBlockInstrument.BASEDRUM).sound(SoundType.DEEPSLATE).strength(55.0F, 1200.0F);
    }

    private static BlockBehaviour.Properties machine() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE)
                .instrument(NoteBlockInstrument.BASEDRUM).sound(SoundType.DEEPSLATE)
                .noOcclusion().strength(4.5F, 1200.0F);
    }

    public static final RegistryObject<Block> REINFORCED_POLISHED_DEEPSLATE = block("reinforced_polished_deepslate", () -> new Block(reinforced()));
    public static final RegistryObject<Block> REINFORCED_DEEPSLATE_BRICKS = block("reinforced_deepslate_bricks", () -> new Block(reinforced()));
    public static final RegistryObject<Block> REINFORCED_DEEPSLATE_TILES = block("reinforced_deepslate_tiles", () -> new Block(reinforced()));
    public static final RegistryObject<Block> REINFORCED_DEEPSLATE_PILLAR = block("reinforced_deepslate_pillar", () -> new RotatedPillarBlock(reinforced()));
    public static final RegistryObject<Block> DEEPSLATE_PILLAR = block("deepslate_pillar", () -> new RotatedPillarBlock(BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).instrument(NoteBlockInstrument.BASEDRUM).sound(SoundType.DEEPSLATE).strength(4.5F, 1200.0F)));
    public static final RegistryObject<Block> SOUL_LAMP = block("soul_lamp", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).instrument(NoteBlockInstrument.BASEDRUM).sound(SoundType.DEEPSLATE).strength(4.5F, 1200.0F).lightLevel(s -> 15)));
    public static final RegistryObject<WaterloggedGrateBlock> REINFORCED_GRATE = block("reinforced_grate", () -> new WaterloggedGrateBlock(reinforced().noOcclusion()));
    public static final RegistryObject<Block> REINFORCED_CHAIN = block("reinforced_chain", () -> new ChainBlock(BlockBehaviour.Properties.copy(Blocks.CHAIN).strength(55.0F, 1200.0F)));

    public static final RegistryObject<StairBlock> REINFORCED_POLISHED_DEEPSLATE_STAIRS = stair("reinforced_polished_deepslate_stairs", REINFORCED_POLISHED_DEEPSLATE);
    public static final RegistryObject<StairBlock> REINFORCED_DEEPSLATE_BRICK_STAIRS = stair("reinforced_deepslate_brick_stairs", REINFORCED_DEEPSLATE_BRICKS);
    public static final RegistryObject<StairBlock> REINFORCED_DEEPSLATE_TILE_STAIRS = stair("reinforced_deepslate_tile_stairs", REINFORCED_DEEPSLATE_TILES);
    public static final RegistryObject<SlabBlock> REINFORCED_POLISHED_DEEPSLATE_SLAB = slab("reinforced_polished_deepslate_slab", REINFORCED_POLISHED_DEEPSLATE);
    public static final RegistryObject<SlabBlock> REINFORCED_DEEPSLATE_BRICK_SLAB = slab("reinforced_deepslate_brick_slab", REINFORCED_DEEPSLATE_BRICKS);
    public static final RegistryObject<SlabBlock> REINFORCED_DEEPSLATE_TILE_SLAB = slab("reinforced_deepslate_tile_slab", REINFORCED_DEEPSLATE_TILES);
    public static final RegistryObject<WallBlock> REINFORCED_POLISHED_DEEPSLATE_WALL = wall("reinforced_polished_deepslate_wall");
    public static final RegistryObject<WallBlock> REINFORCED_DEEPSLATE_BRICK_WALL = wall("reinforced_deepslate_brick_wall");
    public static final RegistryObject<WallBlock> REINFORCED_DEEPSLATE_TILE_WALL = wall("reinforced_deepslate_tile_wall");
    public static final RegistryObject<ReinforcedBarBlock> REINFORCED_BARS = block("reinforced_bars", () -> new ReinforcedBarBlock(reinforced().noOcclusion()));

    // Functional 1.20.1 block-entity ports.
    public static final RegistryObject<HologramBlock> DEEPSLATE_HOLOGRAM = block("deepslate_hologram", () -> new HologramBlock(machine()));
    public static final RegistryObject<TeleporterBlock> DEEPSLATE_TELEPORTER = block("deepslate_teleporter", () -> new TeleporterBlock(machine()));
    public static final RegistryObject<ACTrialSpawnerBlock> DEEPSLATE_SPAWNER = block("deepslate_spawner", () -> new ACTrialSpawnerBlock(
            machine().strength(50.0F, 1200.0F).lightLevel(state -> {
                if (!state.hasProperty(ACTrialSpawnerBlock.STATE)) return 0;
                return switch (state.getValue(ACTrialSpawnerBlock.STATE)) {
                    case WAITING_FOR_PLAYERS, ACTIVE, WAITING_FOR_REWARD_EJECTION, EJECTING_REWARD -> 8;
                    default -> 0;
                };
            })));
    public static final RegistryObject<TrialVaultBlock> DEEPSLATE_VAULT = block("deepslate_vault", () -> new TrialVaultBlock(
            machine().strength(-1.0F, 3600000.0F)));

    private static <T extends Block> RegistryObject<T> block(String name, Supplier<T> factory) {
        RegistryObject<T> registered = BLOCKS.register(name, factory);
        ACItems.ITEMS.register(name, () -> new BlockItem(registered.get(), new Item.Properties()));
        return registered;
    }

    private static RegistryObject<StairBlock> stair(String name, RegistryObject<Block> base) {
        return block(name, () -> new StairBlock(base.get().defaultBlockState(), BlockBehaviour.Properties.copy(base.get())));
    }

    private static RegistryObject<SlabBlock> slab(String name, RegistryObject<Block> base) {
        return block(name, () -> new SlabBlock(BlockBehaviour.Properties.copy(base.get())));
    }

    private static RegistryObject<WallBlock> wall(String name) {
        return block(name, () -> new WallBlock(BlockBehaviour.Properties.copy(Blocks.COBBLESTONE_WALL)));
    }

    private ACBlocks() {}
}
