# Archaion Forge 1.20.1 Port — Phase 6 Big Batch

Target: Minecraft 1.20.1 / Forge 47.x / Java 17
Dependency used directly by this source tree: AAA Particles 2.2.3 Forge 1.20.1

## Completed in this batch

### Last of Deepslate combat loop
- Ported the missing Roll action into native 1.20.1 AI.
- Preserved the original 30-tick windup, rolling window, acceleration/braking curve, repeated shockwave damage, sounds, steering, stuck recovery and phase-sensitive reuse delay.
- Ported the missing Spawn Archaics phase-transition action.
- First transition occurs at <=50% health, advances to phase 1 and regenerates over 40 ticks.
- Second transition occurs at <=50% health after phase 1 and advances to phase 2.
- Player-scaled wave counts preserved: phase 1 = 4 + 6 per extra player; phase 2 = 5 + 7 per extra player.
- Three spawn batches preserved at ticks 45/50/64.
- Archaic type weights preserved, including Brave only appearing in the second transition.
- Spawned Archaics are charged, bound to the boss UUID and receive the original 1.1x/1.2x stat scaling.
- The boss's pre-existing Archaic protection, lethal-damage gate and anti-burst system now have the actual raid generator feeding them.
- Corrected the transition smash sequence to the original three 7.5-block impact points (+7.5 degrees, -7.5 degrees, forward), original blast sizes, 1.0x/1.0x/1.2x damage factors, particles and knockback.

One small presentation/gameplay detail still deferred: the 1.21 EquipmentTable API used to equip raid Wights/Slated does not exist in 1.20.1. The mobs and combat work; their special raid gear will be translated separately rather than emulated through a brittle API hack.

### Teleporter
- Restored the real TeleporterBlock and TeleporterBlockEntity.
- Eight original colors preserved.
- Creative right-click cycles color.
- Same-color nearest loaded teleporter pairing preserved.
- 60-second/1200-tick source+destination cooldown preserved.
- Server-side player beam detection and actual teleport are functional.
- Beam obstruction height is calculated and cached every 10 ticks instead of scanned every tick (intentional server-performance improvement).
- AAA teleport particles and original warp sound preserved.
- Cooldown NBT + client update packets restored.
- Added a Forge 1.20.1 teleporter renderer: colored animated beam while active and floating cooldown readout while disabled.

### Hologram
- Restored the real HologramBlock and HologramBlockEntity.
- Text NBT persistence and block-entity sync restored.
- Original text color retained.
- Added a 1.20.1 renderer with bobbing, player-facing translated text, distance fade, full-bright rendering and outline.
- The original decorative cone/line geometry is deliberately left for the final visual parity pass; text functionality is present now.

### Registry/bootstrap
- Registered hologram and teleporter block entity types under the original `hologram` and `teleporter` IDs.
- Hooked both block entity renderers into the Forge client registration event.
- Replaced Phase 1 generic placeholder blocks for hologram/teleporter with their real classes.

## Trial Spawner / Vault boundary

The original 1.21.1 classes directly extend/use Minecraft's 1.21 `TrialSpawner`, `TrialSpawnerState`, `VaultBlock`, `VaultBlockEntity` and `VaultConfig` Java APIs. Those classes do not exist in vanilla 1.20.1.

This batch intentionally does NOT compile guessed Vanilla Backport package names into Archaion. The current public Vanilla Backport 1.20.1 material does not provide enough evidence that it exposes Mojang's 1.21 trial/vault Java API as a compatibility surface. The safer port is therefore to implement Archaion's deepslate trial spawner/vault as native 1.20.1 Forge block entities (or bind to a verified API only after inspecting the exact installed dependency).

The original 1.21 trial/vault classes and data remain preserved under `port-notes/original-data-1.21.1` for that next conversion.

## Remaining major work
1. Deepslate trial spawner + vault native 1.20.1 implementation.
2. Echo Mace + Echo's Grace + networking/data-component translation.
3. Ancient Keep/worldgen/data conversion and structure processors.
4. Full custom entity models/animations/render layers + boss music networking.
5. Final ForgeGradle compile/runtime integration pass and production JAR.

## Verification status
- Source/API translation and bytecode comparison performed for the systems above.
- AAA Particles 2.2.3 API signatures were checked directly against the supplied Forge 1.20.1 JAR.
- Obvious NeoForge/1.21-only symbols are excluded from active `src/main/java`.
- This package is still a source-stage port, not a claimed fully compiled/tested release JAR.
