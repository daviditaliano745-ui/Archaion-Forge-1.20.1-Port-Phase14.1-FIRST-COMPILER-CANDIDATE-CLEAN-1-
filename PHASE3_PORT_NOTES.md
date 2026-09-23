# Archaion Forge 1.20.1 Port — Phase 3

Implemented in this phase:

- Brave entity registration and functional spawn egg.
- Brave original base attributes: 55 HP, 0.35 movement, 48 follow range, 12 attack.
- Brave charged/owner synced state and NBT persistence.
- Brave target filtering (players + iron golems only).
- Brave target spreading for charged mobs sharing the same owner.
- Brave 18-block retreat spacing and 15-block too-close test.
- Brave leap attack translated from the 1.21 ActionManager into a native Forge AI goal.
  - 15-tick windup.
  - Original fallback leap velocity formula.
  - Landing AoE dimensions, 12 base damage and 1.8 outward knockback.
  - BRAVE_JUMP and custom ambient/hurt/death sounds retained.
  - 1.21 mace smash feedback is temporarily represented with a 1.20.1 explosion sound + POOF particles.
- Grimoray entity registration and functional spawn egg.
- Grimoray original base attributes: 24 HP, 0.4 move/fly, 64 follow range.
- Grimoray POISON_CLOUD / HARMING / HEALING synced variants and NBT persistence.
- Random Grimoray variant on spawn.
- Original orbiting flight + 3-block same-species separation behavior.
- Original spell cooldowns: poison 70t, harming 60t, healing 80t.
- Healing cast: heals Enemy mobs in a 16-block area (self 4 HP, others 3 HP), matching the original predicate.
- AAA Particles healing effects retained using the supplied Forge 1.20.1 AAA Particles 2.2.3 API.
- Grimoray spell projectile registration.
- Original poison cloud (radius 3 / 100 ticks / 10 tick wait / Poison 100t).
- Original harming burst with instant-harm application in a 2-block radius.
- Original poison/harming AAA Particles projectile trails.
- Original 50-tick projectile lifetime and 0.05 projectile gravity.

Still intentionally deferred:

- Original Brave/Grimoray custom models, textures-at-runtime selection, glow layers and animation framework. Gameplay is wired first; Grimoray and its projectile currently use a safe invisible placeholder renderer, while Brave uses a temporary vanilla-compatible renderer.
- Deepslate Sentinel / Haunter / Last of Deepslate.
- Echo Mace and Echo's Grace 1.21 data-component/tool-component logic.
- Trial spawner/vault integration with Vanilla Backport.
- Remaining worldgen/mixins/networking/client animation systems.

This is a source port phase, not yet a release-ready mod JAR.
