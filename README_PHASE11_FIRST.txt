ARCHAION FORGE 1.20.1 PORT — PHASE 11 PRESENTATION-PARITY PRETEST SOURCE
============================================================================

This is the cumulative Phase 1–11 source project for the Forge 1.20.1 backport.
It continues directly from the Phase 10 integration/pretest source.

Phase 11 restores several original renderer behaviours that Phase 10 deliberately
left on generic first-launch renderers, while also auditing more of the original
1.21.1 client implementation for real vs. apparent missing features.

IMPORTANT: this is still SOURCE, not a compiled mod JAR. A real ForgeGradle build
has not been run in this environment because the Gradle wrapper / ForgeGradle /
Minecraft dependency graph is not cached here and outbound dependency fetching
from the build container is unavailable.

Do not rename this ZIP to .jar or place it in the mods folder.
See PORT_STATUS_PHASE11.md and BUILDING.md.
