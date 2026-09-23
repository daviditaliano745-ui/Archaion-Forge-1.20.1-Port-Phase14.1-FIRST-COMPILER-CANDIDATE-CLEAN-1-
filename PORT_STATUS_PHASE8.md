# Archaion Forge 1.20.1 Port — Phase 8 Big Batch

## Scope completed in this batch

### Ancient Keep/worldgen
- Backported the Ancient Keep worldgen definition to Forge/Minecraft 1.20.1.
- Added a custom `archaion:ancient_keep` structure type which delegates generation to the vanilla 1.20.1 jigsaw placement engine while allowing Archaion's original jigsaw depth of **15** (vanilla 1.20.1's JigsawStructure data codec only accepts 0–7).
- Preserved the original start pool, start jigsaw name, start-height range, 110-block max distance, surface projection, spawn overrides, and random-spread structure-set placement.
- Replaced 1.21-only `encapsulate` terrain adaptation with 1.20.1 `beard_box`.
- Removed 1.21-only `dimension_padding` and `liquid_settings` fields.
- Removed the original avoid-trial-chambers placement dependency; vanilla 1.20.1 has no native trial chambers to avoid.

### Structure templates
- Converted **20 valid Ancient Keep NBT templates** for 1.20.1 and set DataVersion to 3465.
- `misc_room.nbt` and `misc_room_x.nbt` were confirmed to be zero-byte files in the original uploaded Archaion JAR, so their pool entries were removed instead of inventing replacement rooms.
- Removed 1.21-only jigsaw priority fields and data-component payloads.
- Rewrote embedded `minecraft:vault` block-entity ids to Archaion's native 1.20.1 `archaion:trial_vault` block entity.
- Preserved trial-spawner `spawn_data` and equipment-profile data so the native Phase-7 trial-spawner runtime can consume the original structures unchanged.

### Datapack conversion
- Converted the 1.21 singular registry folders to the folder layout expected by 1.20.1 where required: `recipes`, `loot_tables`, `advancements`, and `structures`.
- Converted recipes to 1.20.1 result syntax.
- Added optional `forge:maces` compatibility so the Echo Mace upgrade recipe does not hard-require the 1.21 vanilla mace.
- Converted active loot tables and removed 1.21-only loot-function fields.
- Converted all ten Archaion advancements to 1.20.1 predicate/icon forms.
- Added the Ancient Keep structure tag used by the exploration map.
- Converted the Ancient Keep processor list to its 1.20.1-compatible vanilla processors; the old 1.21 vault processor is no longer required because vault NBT is converted offline.

### Ancient City map discovery
- Added a Forge 1.20.1 Global Loot Modifier serializer and two explicit modifiers for normal Ancient City chests and Ancient City ice boxes.
- These roll Archaion's existing Ancient Keep exploration-map loot table without recursively re-running global loot modifiers.

### Structure compatibility fix
- Reintroduced the Reinforced Grate as a proper waterloggable block in 1.20.1. The original Ancient Keep palettes contain `reinforced_grate[waterlogged=true/false]`; leaving it as the previous plain Phase-1 block would make those palette states invalid.

## Static validation
- 157 active JSON files parse successfully.
- 20 converted Ancient Keep structure templates load through the local NBT parser.
- 27 template-pool references (20 unique templates) all resolve.
- All 22 Archaion block ids used by the structures are registered.
- All Archaion entity/block-entity ids found in structure NBT resolve to the corresponding port registries.
- Every converted template has DataVersion 3465.
- No active NeoForge namespace/model-data keys remain.
- No active 1.21 `dimension_padding`, `liquid_settings`, data-component API names, `CustomPacketPayload`, or `StreamCodec` references remain.
- No converted template retains `minecraft:vault`, 1.21 jigsaw priority fields, or 1.21 vault server/shared data.

## Compile status
This is still a source port, not a claimed compiled release. The current container has Java 21 but no Gradle/ForgeGradle dependency cache or usable outbound Gradle dependency resolution, so a genuine ForgeGradle compile has not yet been completed here. The worldgen API shapes used in this phase were cross-checked against Forge 1.20.1 documentation/Javadocs, and the project remains configured for Java 17 / Forge 47.2.30.

## Main remaining work
1. Port the original custom entity models, animation/controller layer, charged visual layers, projectile presentation, boss presentation/music, and any client-only rendering still represented by safe placeholders.
2. Restore any remaining original networking that is actually needed after the 1.20.1 architecture changes.
3. Full Forge compile/integration pass and runtime testing: registry load, datapack reload, `/locate structure archaion:ancient_keep`, actual Keep generation, spawner/vault progression, boss fight, Echo weapons, and client/server joins.
4. Fix compiler/runtime issues exposed by that pass and produce the final playable JAR.
