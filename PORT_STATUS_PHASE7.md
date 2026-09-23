# Archaion Forge 1.20.1 Port — Phase 7 Big Batch

Target: Minecraft 1.20.1, Forge 47.2.30+, Java 17.
Dependency bundled for development: AAA Particles 2.2.3 Forge 1.20.1.

## Added in this batch

### Native 1.20.1 Deepslate Trial Spawner
- Replaces the missing vanilla 1.21 TrialSpawner runtime rather than requiring its Java API.
- Preserves original blockstate property names so original Archaion models remain usable.
- Reads original Ancient Keep structure NBT (`spawn_data.entity.id` and equipment loot-table profile).
- Original configuration preserved: spawn range 4, base total 6, simultaneous 2, +1 total/player, +0.5 simultaneous/player, 100 ticks between spawns, 14-block player range, 36,000-tick cooldown.
- Tracks spawned mobs and participating players.
- Uses original normal reward weighting for Echo Key vs misc reward ejection.
- Equipment profiles are reconstructed as gameplay-equivalent 1.20.1 loadouts; trim/enchantment cosmetics remain for the data/client pass.

### Native 1.20.1 Deepslate Vault
- Per-player redemption tracking.
- Echo Key requirement/consumption.
- Original 4.0/4.5 activation/deactivation ranges.
- Unlock/ejection states and five reward rolls.
- Original reward weights preserved.
- Optional Density compatibility: if a 1.20.1 backport registers `minecraft:density` or `vanillabackport:density`, Density books are automatically restored to the original 1/41 reward slot without a hard dependency.

### Echo Mace
- Replaces unavailable 1.21 MaceItem/data-component dependency with a native 1.20.1 Item implementation.
- +7 attack damage and -3.2 attack speed main-hand attributes.
- Spear-use animation and five-tick minimum charge.
- 1 durability consumed per throw, 30-tick cooldown, original 2.5 velocity / 1.0 inaccuracy.
- Dedicated synced ThrownEchoMace entity.
- Direct impact damage starts at 8 and grows by 0.5 per projectile tick, matching the original baseline.
- Block-hit reflection preserves the original 1.75 dot-product bounce calculation and four-impact lifetime.
- AAA Particles Echo Blast effect retained.
- 1.21-only vanilla Mace enchantment effect-component behavior (notably Wind Burst/Density hooks) remains a later optional-backport integration item.

### Echo's Grace
- Functional 1.20.1 weapon implementation.
- Original 20-tick baseline draw, 15-block default design range, 12 base Echo Star damage, Power bonus, Quick Charge timing, Multishot three-projectile spread, repair via Echo Shard, enchantability 15.
- Does not require consuming physical Echo Charge ammo, matching the original implementation's generated projectile-stack behavior.
- Existing pulling model overrides now work through registered 1.20.1 item predicates.

### Client/resource compatibility
- Echo Mace model parent changed from 1.21-only `minecraft:item/handheld_mace` to 1.20.1 `minecraft:item/handheld`.
- NeoForge model face key `neoforge_data` converted to Forge 1.20.1 `forge_data` across preserved glowing models.
- Thrown Echo Mace registered with ThrownItemRenderer.

## Validation performed
- Active Java source scanned for NeoForge package imports and obvious 1.21-only runtime classes/data components.
- Entity builder/client setup signatures cross-checked against Forge 1.20.1 API documentation.
- Original 1.21 bytecode for EchoMaceItem, EchosGraceItem and ThrownEchoMace saved under port-notes/original-javap for future parity checks.
- Existing Archaion model predicates and Trial/Vault blockstate property names cross-checked against preserved assets/structure NBT.

## Important limitation
This package is source, not yet a claimed production JAR. A full ForgeGradle compile/run pass still has to happen once a Forge 1.20.1 development classpath is available. The current environment could reach Forge documentation but could not download/resolve the MDK/Gradle dependency graph into the local container, so no fake "compiled" claim is made.

## Main work still left
- Ancient Keep/worldgen/structure placement + 1.21 datapack conversion to 1.20.1 formats.
- Remaining original loot/recipe/advancement/tag conversion and smithing-template behavior.
- Boss/client networking (music, shake, bossbar dynamic data) where still needed.
- Full entity models, animation system and charged/emissive render layers; current gameplay entities use temporary safe rendering where necessary.
- Trial-spawner internal mob preview rendering and remaining visual parity.
- Optional Vanilla Backport integration for Density/Wind Burst/mace-specific mechanics.
- Final ForgeGradle compile, dedicated-server/client launch, registry/datapack validation, runtime testing and production JAR.
