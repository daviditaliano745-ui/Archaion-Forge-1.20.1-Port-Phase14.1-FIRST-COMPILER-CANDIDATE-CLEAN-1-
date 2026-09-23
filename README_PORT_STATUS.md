# Archaion -> Forge 1.20.1 port (cumulative through Phase 4)

This is the first real port slice reconstructed from the supplied Archaion 1.21.1-1.4.3 JAR and the supplied AAA Particles 2.2.3 Forge 1.20.1 JAR.

## Completed in this slice
- Forge 47.x / Java 17 project bootstrap.
- `@Mod` entry point rewritten from NeoForge constructor injection to Forge 1.20.1's mod event bus.
- Original Archaion assets and data copied into the project unchanged for preservation.
- AAA Particles 2.2.3 Forge 1.20.1 included as the local development dependency.
- Block registry IDs preserved.
- Item registry IDs preserved.
- Sound registry IDs preserved.
- Armor Break effect ported to 1.20.1's attribute-modifier API.
- Creative tab ported.
- Reinforced Bars custom block logic reconstructed from the original bytecode.

## Deliberately not called finished yet
The current `echo_mace`, `echos_grace`, spawn eggs, hologram and teleporter registrations are temporary compile-time placeholders. Their registry IDs are correct, but their special behavior is not yet ported.

The next required slices are:
1. entity classes + entity registry and spawn eggs;
2. hologram/teleporter block entities and render hooks;
3. Echo Mace / Echo's Grace / projectile code, replacing 1.21 data components with 1.20.1 NBT/capability-compatible state where necessary;
4. Forge SimpleChannel networking replacement for NeoForge payload registration;
5. trial spawner/vault compatibility layer using Vanilla Backport where its API can replace 1.21 vanilla classes;
6. worldgen structure/processor/placement port;
7. mixins rewritten for Java 17 / 1.20.1 targets;
8. client renderers, particles and final integration test.

Do not treat this phase as a drop-in gameplay-ready release yet. It is the stable base for the remainder of the backport.

### Phase-1 safety cleanup
The original 1.21.1 data pack is preserved under `port-notes/original-data-1.21.1/` but is **not active yet**. 1.21.1 renamed several data-pack directories and changed recipe/advancement/worldgen codecs; enabling those files unchanged on 1.20.1 would create misleading loader errors. They will be restored subsystem-by-subsystem as each matching Java registry/codec is ported.

The original access transformer is likewise preserved under `port-notes/original-meta-1.21.1/`; it contains direct references to 1.21-only classes such as `MaceItem` and newer ender-pearl internals and is intentionally not active in Phase 1.

## Phase 2 additions
Functional 1.20.1 ports now exist for Slated, Wight and Impact Pearl, including real entity registration, attributes, synced charged state/NBT, spawn eggs and temporary compatible renderers. See `port-notes/PHASE2.md`.

## Phase 3 additions

Brave and Grimoray are now gameplay-ported, including their native 1.20.1 AI replacements, spawn eggs, entity registrations, attributes, Grimoray spell projectile, AAA Particles effects, and spell/leap behavior. See `PHASE3_PORT_NOTES.md` for the exact preserved behavior and remaining client/model work.


## Phase 4 additions

Deepslate Sentinel is now gameplay-ported: registry, attributes, spawn egg, charged/owner state, Wight pickup/passenger behavior, and its full timed charge/shockwave attack are implemented as native Forge 1.20.1 AI. AAA Particles charge impacts and the original sound cadence are retained. Its bespoke large-entity path navigation and custom renderer/animation remain deferred. See `PHASE4_PORT_NOTES.md`.
