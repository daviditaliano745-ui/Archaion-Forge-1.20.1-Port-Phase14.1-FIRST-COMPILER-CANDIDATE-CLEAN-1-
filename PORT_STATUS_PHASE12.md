# Archaion Forge 1.20.1 Port — Phase 12 Models / Trial Spawner Pretest

## Starting point
Phase 12 is cumulative over the Phase 11 presentation-pretest source. It preserves
all Phase 1–11 gameplay, worldgen, Trial Spawner/Vault, item, network, AAA Particles,
performance-oriented native AI, and renderer work.

## Changes in Phase 12

### Authored major-mob geometry restored
The generic Phase 10 fallback mob geometry has been removed from active client
registration. Forge 1.20.1 model classes now carry the authored geometry for:
- Brave
- Deepslate Sentinel
- Grimoray
- Haunter
- Last of Deepslate

Their renderers now use the original entity textures and appropriate phase/variant
selection. Lightweight native idle/walk/sleep presentation is supplied without
reintroducing the original custom ActionManager tick/runtime architecture.

### Slated and Wight fallback renderers removed
Slated now uses the native 1.20.1 zombie renderer/armor geometry with its Archaion
texture, emissive glow, and charged overlay. Wight now uses native skeleton geometry
with armor, held-item compatibility, glowing eyes, and charged overlay.

### Charged model shells restored
Brave, Haunter, and Last of Deepslate now register and render the separately inflated
charged model layers used by the original presentation (CubeDeformation 2.0). Their
charged shell receives the live entity model properties and animation setup each
frame. Deepslate Sentinel, Slated, and Wight correctly reuse their normal geometry.

### Trial Spawner display mob restored
The native 1.20.1 Trial Spawner block entity now maintains a client-only cached
display entity and interpolation state. Its block-entity renderer restores the
rotating, tilted, centered/scaled preview mob while leaving the custom 1.20.1 Trial
Spawner gameplay implementation intact.

### Phase 11 renderer parity retained
The exact thrown Echo Mace presentation, visible tumbling Last of Deepslate terrain
chunks, intentionally invisible effect-only helper entities, and verified Echo's
Grace free-fire behavior remain intact.

## Deliberately not reintroduced before first launch
The original 1.21 custom ActionManager/animation-manager subsystem is still replaced
by the port's native AI/attack architecture. Exact authored attack keyframe playback
is therefore not yet a requirement for the first integration test. Reintroducing the
old manager before profiling would risk undoing the event/native-AI performance work.
Geometry, textures, charged shells, idle/walk/sleep presentation, and gameplay logic
are present; exact attack-animation polish can be revisited from real runtime evidence.

Ambient Ancient Keep fog and the custom boss-bar skin also remain post-first-launch
presentation work rather than compile blockers.

## Cumulative static validation after Phase 12
- Active Java source files: 93.
- JSON/pack metadata files parsed: 160; parse errors: 0.
- Ancient Keep NBT templates checked: 20; structural read errors: 0.
- Local Archaion resource references checked: 482; missing targets: 0.
- Active NeoForge references in Java/resources: 0.
- Active mixin configs/references: 0.
- No-classpath Java 17 syntax-level diagnostics: 0.

## Build/test status
A real ForgeGradle compile has NOT been run and no mods-folder JAR is claimed. The
available container has no ForgeGradle/Gradle wrapper/dependency graph and cannot
fetch the missing build dependencies. Library search also found runtime logs but no
reusable Forge MDK/Gradle development environment.

The next hard gate is therefore still:
1. Real ForgeGradle `build`.
2. Fix any compile/API errors exposed by the actual Forge classpath.
3. Launch a minimal client.
4. Launch a dedicated server.
5. Produce the first test JAR only after the real build succeeds.
