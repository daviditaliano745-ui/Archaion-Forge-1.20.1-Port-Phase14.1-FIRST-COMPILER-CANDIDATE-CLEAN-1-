ARCHAION FORGE 1.20.1 PORT — PHASE 12 MODELS / TRIAL SPAWNER PRETEST SOURCE
================================================================================

This is the cumulative Phase 1–12 source project for the Forge 1.20.1 backport.
It continues directly from the Phase 11 presentation-pretest source.

Phase 12 replaces the remaining generic major-mob presentation fallbacks with the
authored geometry/render paths, restores Slated/Wight native renderers, restores the
inflated charged shells, and reinstates the Trial Spawner rotating display mob.

IMPORTANT: this is still SOURCE, not a compiled mod JAR. A real ForgeGradle build
has not been run in this environment because the Gradle wrapper / ForgeGradle /
Minecraft dependency graph is not cached here and outbound dependency fetching from
the build container is unavailable.

Do not rename this ZIP to .jar or place it in the mods folder.
See PORT_STATUS_PHASE12.md and BUILDING.md.
