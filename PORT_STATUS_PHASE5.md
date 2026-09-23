# Archaion Forge 1.20.1 Port — Big Batch 1 / Phase 5

Target: Minecraft 1.20.1, Forge 47.x, Java 17
AAA Particles target: Forge 1.20.1 2.2.3 (user-supplied dependency)

## Added in this batch

### Haunter
- Functional entity type + spawn egg + attributes.
- Charged/owner persistence.
- Original targeting restrictions (players / iron golems).
- Native 1.20.1 swelling/explosion AI replacing ActionManager.
- 40-tick detonation timing, 60-tick action length, 100–160 cooldown.
- 15 explosion damage, Armor Break II-equivalent amplifier 1 for 200 ticks, outward knockback.
- AAA `haunter_boom` and original sound hooks.

### Last of Deepslate — boss core
- Functional entity type + spawn egg + attributes (600 HP, 35 attack, 64 follow range, 15 armor).
- Sleeping / waking / awake synced state using vanilla INT entity data instead of the 1.21 custom serializer.
- Four-Echo-Charge wake sequence and 80-tick wake transition.
- Native ServerBossEvent.
- ArchaicRaid system ported: owned-Archaic detection, charged-Archaic counting, intended raid count, phase persistence.
- Original charged-Archaic protection curve and cannot-kill-while-protected rule.
- Original player-count-sensitive anti-burst damage multiplier.
- Phase-dependent Armor Break on melee hits.

### Last of Deepslate — physical attacks
- Swing/spin: timing, 0.9x damage, 1.5 knockback, AAA `echo_spin`.
- Body slam: jump/ground snap timing, 1.15x damage, large knockback, AAA `lod_boom_ground`.
- Phase-3 expanding slam ring via `LODSlamEffect` (12 arms, chained generations, 30 damage nodes).
- Ground smash: original range rules, tick 27 damage, Hard 1.075x multiplier, forward impact volume.
- `LODFallingBlock` gameplay support for phase 2+ ground-smash terrain chunks.
- Falling-block source selection uses heightmap lookup rather than the original per-column 64-block scan to avoid an unnecessary server-performance spike while preserving the visible/gameplay intent.

### Last of Deepslate — ranged package
- Echo Star projectile: 20 base damage + power bonus, 3-block AoE, 0.02 gravity, 100-tick lifetime, AAA effects.
- Echo Star can feed a sleeping Last of Deepslate on direct hit, matching the original special interaction.
- Intercept Blast: repeating every-2-tick AoE, 55*size damage, 14*size box, knockback, AAA intercept blast.
- Normal ranged action: original blast-vs-star random branch, tick 21 / 23 / 25 volley timings, phase-dependent shooting cooldown.
- Anti-air intercept action: tick-14 upward blast + five Echo Stars, 500-tick action cooldown.

## Still not ported / intentionally deferred
- LOD roll action.
- LOD spawn-Archaics raid action (this is the phase-transition/spawn choreography layer; ArchaicRaid state itself is already ported).
- Boss death animation/music packets and custom boss-bar supplemental data.
- Custom large-entity navigation/control classes.
- Original 1.21 model/animation/render stack (safe invisible renderers remain for custom mobs/projectiles).
- Trial/vault/teleporter/hologram systems.
- Echo Mace / Echo's Grace component/networking systems.
- Ancient Keep worldgen/data conversion.
- Full ForgeGradle compile/runtime integration pass.

## Validation performed in this environment
- Removed/checked for NeoForge imports and common 1.21-only symbols in active Java source.
- Verified new entity registry IDs against the original compiled JAR (`haunter`, `last_of_deepslate`, `echo_star`, `lod_falling_block`, `lod_intercept_blast`, `lod_slam`).
- Braces/source syntax structural pass completed.
- `javac` parser pass found no Java syntax errors; unresolved Minecraft/Forge symbols are expected because this environment does not currently have the ForgeGradle-resolved Minecraft classpath.

This package is source-stage, not claimed as a runtime-tested production JAR yet.
