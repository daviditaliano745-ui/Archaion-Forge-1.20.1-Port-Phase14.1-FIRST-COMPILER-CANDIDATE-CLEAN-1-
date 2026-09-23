# Archaion Forge 1.20.1 Port — Phase 2

Implemented in this slice:

- Real Forge `EntityType` registry introduced.
- `Slated` ported to 1.20.1: charged synced state, owner UUID persistence, original 30 HP / 10 damage / 50 follow range, no baby form, no water conversion, and melee slowness effect.
- `Wight` ported to 1.20.1: charged synced state, owner UUID persistence, 22 HP / 0.25 speed, weakness arrows with 6 base damage, and Stray sound set.
- `Archaic` interface ported, including the original x20 charged XP helper.
- `ThrownImpactPearl` and `ImpactPearlItem` ported with the original cooldown, throw velocity, area damage scaling and knockback.
- Slated/Wight spawn eggs changed from placeholders to `ForgeSpawnEggItem`.
- Common entity attribute registration added.
- Temporary vanilla renderers added so the Phase 2 mobs/projectile have a valid client renderer before the custom animation/model pass.

1.20.1 compatibility substitution:

- 1.21's mace heavy-smash sound/event does not exist in 1.20.1. Impact Pearl currently uses vanilla explosion/POOF feedback while keeping the gameplay calculation. This is intentionally temporary and can later be swapped to an Archaion/AAA Particles effect.
- Wight's 1.21 three-argument skeleton arrow hook was adapted to the 1.20.1 two-argument hook.
- Slated's 1.21 synced-data Builder API was adapted to 1.20.1 `defineSynchedData()` / `entityData.define()`.

Still pending:

- Brave, Grimoray, Deepslate Sentinel, Haunter, Last of Deepslate.
- Echo Star, Grimoray spell, Echo Mace projectile, LOD slam/intercept/falling-block entities.
- Custom entity models/renderers and animation system.
- Full networking/client camera/boss-bar packets.
- Echo Mace / Echo's Grace item systems.
- Trial spawner/vault + Vanilla Backport integration.
- Ancient Keep worldgen conversion.
