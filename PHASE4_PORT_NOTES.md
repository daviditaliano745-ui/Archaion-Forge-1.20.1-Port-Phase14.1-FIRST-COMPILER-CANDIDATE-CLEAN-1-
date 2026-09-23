# Archaion Forge 1.20.1 Port — Phase 4

Implemented in this phase:

- Deepslate Sentinel entity registration and functional Forge spawn egg.
- Original entity dimensions retained where 1.20.1 supports them: 2.75 x 2.6, tracking range 10.
- Original combat attributes:
  - 100 max health
  - 0.30 movement speed
  - 15 attack damage
  - 8 armor
  - 4 armor toughness
  - 1.0 knockback resistance
  - 48 follow range
- Charged state and owner UUID sync/NBT persistence retained.
- Original custom Sentinel ambient/hurt/death/start-charging sounds retained.
- Wight passenger system ported:
  - scans up to 32 blocks for an alive non-riding Wight with line of sight;
  - paths to the nearest valid Wight;
  - Wight mounts within the original distance-squared < 6 threshold;
  - Sentinel accepts only one Wight passenger;
  - normal controlling-passenger movement is suppressed while a nearby Wight is waiting for pickup.
- Sentinel charge translated from the 1.21 ActionManager into a native 1.20.1 Goal:
  - cannot start during its cooldown, without a living visible target, while carrying a passenger, or while a Wight is waiting for pickup;
  - charge direction is locked from the target at action start;
  - 30-tick windup;
  - acceleration from tick 31 over six ticks to 1.1 horizontal speed;
  - charge runs through tick 120 and decelerates over its final five ticks;
  - repeated impact pulse every 4 ticks;
  - each pulse uses the original 15 explosion-type damage and 3.0 outward + 0.4 upward knockback;
  - original echo_blast_intercept AAA Particles effect retained;
  - original ECHO_STAR_BLAST cadence/pitch jitter retained during the charge;
  - movement-stuck detection stops the charge after >8 nearly-stationary charge ticks;
  - total action envelope remains 140 ticks;
  - original randomized 180-220 tick post-charge cooldown retained.
- Client registration uses a safe invisible renderer until the dedicated model/animation pass.

Deferred from this phase:

- The original LargeEntityPathNavigation/BandaidPathFinder implementation. The Sentinel currently uses standard 1.20.1 ground navigation; the custom large-entity corner/path truncation behavior remains a later navigation-compatibility pass.
- The exact 1.21 model/animation system and charged render layer.
- Haunter and Last of Deepslate.
- Trial/vault/teleporter/hologram block entities.
- Echo Mace / Echo's Grace and networking.
- Worldgen/data codec conversion and final integration testing.

This remains a source-port phase, not a release-ready mod JAR.
