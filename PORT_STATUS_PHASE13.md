# Archaion Forge 1.20.1 Port — Phase 13 Ambience / HUD Integration Pretest

## Starting point
Phase 13 is cumulative over the Phase 12 models/Trial-Spawner pretest source. It preserves all prior gameplay, worldgen, custom Trial Spawner/Vault, items, AAA Particles integration, native-AI performance work, authored mob geometry, renderer layers, and network/audio work.

## Changes in Phase 13

### Ancient Keep ambience restored
- Added the original structure-tag-driven Ancient Keep presence check on the server.
- Player structure checks run once every 10 ticks rather than every tick.
- The Ancient Keep bounding box is synchronized only when a player transitions from outside to inside.
- Client fog ramps by 0.05 per tick and uses smoothstep interpolation.
- Fog distance blends toward near=16 and far=128 while inside the Keep.
- Fog color blends toward the original RGB 0.39 / 0.898 / 1.0.
- The original 12 sculk-soul ambience particles per client tick are restored around the local player.
- Logout cleanup removes the server-side Keep-presence cache entry.

### Last of Deepslate custom HUD restored
- Added compact event-driven boss-bar data synchronization keyed to the vanilla ServerBossEvent UUID.
- Synced values are limited to the four original integer fields: charged-Archaic state, raid alive count, raid total, and phase.
- Raid data is sent only when the alive/total/phase state changes, plus the initial tracking snapshot.
- Tracking stop/death removes the client-side custom boss-bar state.
- Restored the authored sliced/wave boss bar, progress fill, Archaic raid sub-bar, charged overlay, custom outlined boss name, and 42-pixel bar increment.

### Sleeping-boss Echo Charge tooltip restored
- Looking directly at a sleeping Last of Deepslate within 32 blocks now shows the activation tooltip.
- The raycast respects intervening block outlines, so the tooltip does not appear through walls.
- The tooltip displays the Echo Charge item and the remaining number of charges required, with the original bobbing presentation.
- Uses Forge 1.20.1's native client tooltip-component registration rather than a 1.21-only tooltip skin API.

### Boss music resilience restored
- The active Last of Deepslate theme now repairs itself if the client sound manager unexpectedly drops the looping sound while the same boss phase/level remains active.

### Network hardening
- Added Forge SimpleChannel packets for Ancient Keep ambience, custom boss-bar sync, and boss-bar removal.
- Boss-bar map payloads are capped at 16 entries and keys at 64 UTF characters; malformed oversized counts are rejected on decode.
- Client-only packet handlers remain behind DistExecutor boundaries for dedicated-server classloading safety.

### Last of Deepslate movement/control parity restored
- Restored the original turn-aware `ACMoveControl` without bringing back the old ActionManager-facing `ACEntity` interface.
- Preserved Last of Deepslate's original **0.15 rotation freedom**, so it turns with the same deliberately heavy feel while moving.
- Restored the original eased look control: 0.4 yaw interpolation / 60-degree per-tick clamp, 0.25 pitch interpolation / 40-degree clamp, and 40-degree idle head return.
- Preserved the original Smash Ground look freeze through its first 27 ticks by bridging the old ActionManager condition to the native 1.20.1 Smash Ground goal.
- Restored the original random target cycler: it considers non-creative players and iron golems inside a 64-block inflated box and reselects every 100-139 ticks.
- Restored the original **1.5-block step height** using 1.20.1's entity step-height setter.
- Restored the original no-body-push `pushEntities()` behavior.

### Deliberately deferred large-hitbox pathfinder
- The original `LargeEntityPathNavigation` / nested Bandaid pathfinder is still compatibility-deferred. It is a large version-sensitive pathfinding replacement and is the one movement subsystem not safe to transplant blindly without a Forge compile/runtime.
- Last of Deepslate retains the native 1.20.1 navigation plus the existing lower-priority move-to-target goal; Deepslate Sentinel likewise retains standard navigation for the first compile.
- This is not a registration/compile requirement and can be revisited from actual movement evidence once the first test build launches.

## Deliberately retained architecture decision
The original 1.21 custom ActionManager/animation-manager runtime remains replaced by native Forge 1.20.1 AI/attack goals. Exact authored attack keyframe playback is not being reintroduced before the first real launch because doing so would restore the ticking/action subsystem that the port intentionally removed. Gameplay timing, models, textures, charged layers, idle/walk/sleep presentation, HUD, music, and atmosphere are now present; attack-animation polish can be revisited from real runtime evidence if needed.

## Phase 13 validation
- Active Java source files: 110.
- JSON/pack metadata files parsed: 160; parse errors: 0.
- Ancient Keep NBT templates structurally parsed: 20; errors: 0.
- Focused model/texture/sound/template reference audit: missing local targets: 0.
- Active NeoForge references in Java/resources: 0.
- Active mixin configs/references: 0.
- No-classpath Java 17 syntax-level diagnostics: 0.
- Key 1.20.1 API signatures used by the new systems were checked against Forge/Minecraft 1.20.1 documentation/mappings.

## Build/test status
A real ForgeGradle compile has still NOT run, so no mods-folder JAR is claimed yet. The working container has no reusable ForgeGradle wrapper/dependency graph and direct retrieval of the Forge 47.4.20 MDK from Maven failed in this environment. The user's Library contains runtime logs but not a reusable development MDK/Gradle environment.

At this point the source port is essentially frozen for the first compile. The project compile target is Forge 47.4.20 (matching the user's pack), while `mods.toml` keeps the compatibility floor at Forge 47.2.30+ because AAA Particles requires that minimum. The remaining hard gate is:
1. Real ForgeGradle `build` against a normal 1.20.1 Forge development environment (preferably Forge 47.4.20, matching the user's pack).
2. Fix any actual classpath/API errors exposed by that build.
3. Minimal client launch.
4. Dedicated-server launch/classloading check.
5. Produce the first reobfuscated test JAR only after the build succeeds.
