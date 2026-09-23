# Archaion Forge 1.20.1 — Phase 14.2 First Compile Fixes

Apply this patch OVER the existing Phase 14.1 repository, preserving folders and overwriting the four matching Java files.

Fixes:
1. Haunter#getExperienceReward(): changed from protected to public to match Minecraft 1.20.1 Mob API.
2. TrialSpawnerRenderer: removed unsupported BlockEntityRenderer#getRenderBoundingBox override.
3. TeleporterRenderer: removed unsupported BlockEntityRenderer#getRenderBoundingBox override.
4. HologramRenderer: removed unsupported BlockEntityRenderer#getRenderBoundingBox override.

No gameplay/balance/content changes are included.

After copying these files into the repository:
- commit to main
- push origin
- GitHub Actions should automatically run the next Forge 47.4.20 compile.
