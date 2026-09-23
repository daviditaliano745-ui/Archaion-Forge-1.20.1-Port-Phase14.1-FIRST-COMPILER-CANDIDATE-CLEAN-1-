# Archaion Forge 1.20.1 Port — Phase 9 Client / Network Presentation

This is the cumulative Phase 1–9 source tree.

## Added in Phase 9

- Forge 1.20.1 `SimpleChannel` networking replacement for the remaining client presentation packets.
- Last of Deepslate boss music synchronization with three original streamed music tracks and STOP/fade handling.
- Corrected the music registry IDs to the actual asset IDs: `lod_boss_theme_phase_1/2/3`.
- Music phase follows the boss's zero-based raid phase and is resynchronized for players that begin tracking the boss.
- Boss music fades/stops when the player stops tracking the boss or the boss dies.
- Client camera shake packet + local camera shake handler.
- Camera shake hooked into Last of Deepslate wake smash, body slam, ground smash, Archaic transition smashes, and Haunter detonation.
- Type-safe generic 1.20.1 fallback entity model/renderer for Slated, Wight, Brave, Grimoray, Deepslate Sentinel, Haunter, and Last of Deepslate.
- Removed the previous unsafe Zombie/Skeleton renderer casts that could fail through generated bridge methods.
- Grimoray renderer selects the correct poison/harming/healing texture.
- Last of Deepslate renderer selects the correct phase texture.
- Charged energy-swirl presentation restored on the fallback models using the same charged textures the original 1.21 render layers use.
- Existing Teleporter/Hologram block entity renderers and Echo's Grace item predicates remain active.

## Intent of the fallback models

The original mod has bespoke 1.21 animation/model classes and an animation manager. Phase 9 intentionally does not pretend those have been fully reproduced yet. The fallback models make every major gameplay mob and the boss visible and type-safe for the first integration test candidate while preserving the original textures, phase selection, and charged overlays. Exact silhouette/animation parity remains polish work after the port is proven stable.

## Validation performed

- 76 active Java source files enumerated.
- 157 active JSON resources parsed successfully.
- No active `net.neoforged` imports.
- No active 1.21 CustomPacketPayload/StreamCodec/DataComponents networking/data-component symbols.
- All entity textures referenced by Phase 9 renderer registration exist.
- All three Last of Deepslate theme sound-event keys exist in `sounds.json` and their OGG files exist.
- Unsafe vanilla entity-renderer casts removed.
- Delimiter/syntax structural scan across all active Java source passed.
- A no-classpath `javac` smoke pass reports no Java syntax diagnostics. Its unresolved Minecraft/Forge imports are expected without the ForgeGradle classpath.

## Not yet claimed

This is **not** claimed to be a successfully compiled Forge JAR. The build container has Java but has no cached ForgeGradle 1.20.1 dependency graph, and outbound DNS/network resolution is unavailable, so the official 47.2.30 MDK/dependencies cannot currently be fetched here. Full compiler/API/runtime validation is the next integration phase.

## Next milestone

Phase 10 is the build/test-candidate pass: resolve real Forge compiler errors, fix dedicated-server/client classloading issues, resource/data loader failures, and startup/runtime crashes, then produce the first JAR intended for the user's actual Forge 1.20.1 instance.
