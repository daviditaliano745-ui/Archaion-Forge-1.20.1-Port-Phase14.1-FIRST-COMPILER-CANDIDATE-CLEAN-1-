# Archaion Forge 1.20.1 Port — Phase 10 Integration / Pretest Candidate

## Status

This package is the cumulative Phase 1–10 **source project** for the Archaion
1.21.1-1.4.3 -> Forge 1.20.1 backport. Phase 10 intentionally stops adding large
features and concentrates on integration correctness before the first real
ForgeGradle compiler/runtime pass.

It is **not yet a compiled gameplay JAR**. The only remaining hard gate before a
first launch candidate is compiling the project against the real Forge 1.20.1
classpath and fixing any compiler/resource-loader/runtime errors that exposes.

## Phase 10 changes

### Build/project integration
- Normalized `build.gradle` to a ForgeGradle 6 / Minecraft 1.20.1 MDK-style project.
- Java toolchain target is Java 17.
- Minecraft target: 1.20.1.
- Minimum Forge target: 47.2.30.
- Added the user's exact AAA Particles 2.2.3 Forge 1.20.1 JAR as a local deobfuscated dependency.
- Added `pack.mcmeta` with 1.20.1 pack format 15.
- `mods.toml` is resource-expanded from Gradle properties and declares Forge,
  Minecraft, and AAA Particles requirements.
- Pretest source version: `1.20.1-1.4.3-port.test1`.

### Restored missed progression items
- `Echo Charge` is no longer a placeholder Item:
  - 4-tick cooldown;
  - original Last-of-Deepslate shoot sound/pitch behavior;
  - launches an Echo Star at the original speed/inaccuracy;
  - consumes one charge outside Creative;
  - restores the original description/keyword tooltip.
- Both upgrade smithing templates are real 1.20.1 `SmithingTemplateItem`s again,
  with Archaion's custom empty-slot icons and smithing-screen descriptions.

### Armor Break parity fix
- Armor/toughness loss remains -5% total armor and -5% total toughness per level.
- Restored the original extra 7.5%/level weakening of Resistance/enchantment
  damage mitigation as closely as Forge 1.20.1's event API permits.
- Limitation: a hit already reduced completely to zero cannot be reconstructed by
  the post-reduction 1.20.1 hook.

### Dedicated-server/client safety
- Camera shake client handling is isolated behind a nested client-only handler,
  matching the boss-music packet pattern.
- No Minecraft client imports are present in common gameplay/registry classes.

### Original mixin audit
The original 1.21.1 JAR contained three mixins. They are intentionally **not**
carried over as mixins because their responsibilities have already been replaced:
- `BlockEntityTypeMixin`: made Archaion's custom Vault count as vanilla 1.21 Vault.
  The 1.20.1 port has its own Trial Vault block entity/type, so this patch is obsolete.
- `LivingEntityMixin`: ticked Archaion's old `ActionManager`. Ported entities use
  native 1.20.1 AI goals/timed logic instead.
- `MobMixin`: supplied one-shot damage modifiers for the old action framework.
  Ported attack goals apply their intended damage directly.

Avoiding these obsolete mixins reduces cross-version fragility and server tick hooks.

## Cumulative gameplay state
By Phase 10 the source contains the Forge bootstrap/registries, all major Archaion
mobs and Last of Deepslate combat loop, Echo/impact projectiles, native Trial
Spawner and Vault replacements, teleporters/holograms, Echo Mace, Echo's Grace,
Ancient Keep worldgen/data conversion, boss music/camera shake, and safe visible
fallback renderers/charged layers for first-launch testing.

## Known first-test limitations
- Original bespoke 1.21 animation/model runtime has not been transplanted. Major
  mobs use safe visible port models/renderers for gameplay testing.
- Several effect/projectile helper entities remain intentionally invisible because
  their particles/effects carry the gameplay presentation.
- Ancient Keep ambient fog and the original custom boss-bar skin are postponed
  presentation polish. The old ambient-fog implementation did a repeating
  structure-state sync and is not being reintroduced before profiling.
- Exact original large-entity pathfinder polish is deferred; gameplay uses the
  safer 1.20.1 pathing implementation currently in the port.

## Static validation performed in this package
- Active Java source files: 79.
- JSON/pack metadata files parsed: 158; parse errors: 0.
- Converted Ancient Keep NBT templates parsed: 20; read errors: 0.
- Obvious active NeoForge / 1.21-only networking/data-component token scan: clean.
- No Sponge/Mixin configuration is active.
- No-classpath `javac --release 17` pass: no syntax-level diagnostics. Missing
  Minecraft/Forge symbols are expected without the real ForgeGradle classpath.
- AAA Particles dependency metadata verified locally as mod id `aaa_particles`,
  version 2.2.3, Forge 47.2.30+.

## What remains before the user should launch it
1. Run the real ForgeGradle 1.20.1 build with dependency resolution available.
2. Fix any actual mapping/API/compiler errors surfaced by Forge/Minecraft classes.
3. Run a client and dedicated-server smoke test and fix resource/datapack loader errors.
4. Build the reobfuscated mod JAR.
5. Test it in the user's full modpack and iterate from crash/latest.log evidence.

That is an integration/test-fix milestone, not another missing-content phase.
