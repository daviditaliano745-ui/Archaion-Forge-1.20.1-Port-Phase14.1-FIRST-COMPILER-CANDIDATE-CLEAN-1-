ARCHAION FORGE 1.20.1 PORT — PHASE 10 INTEGRATION PRETEST SOURCE
=================================================================

This is the cumulative Phase 1–10 source project for the Forge 1.20.1 backport.
It is the first integration/pretest candidate SOURCE, not a compiled mod JAR.

Phase 10 is an integration pass rather than a feature phase. It restores missed
progression/combat behavior (Echo Charge, smithing-template UI, Armor Break's
magic-reduction weakening), hardens client/server classloading, normalizes the
project to a Forge 47.2.30 / Java 17 MDK-style build, and validates the full
cumulative resources/worldgen tree.

IMPORTANT: A full ForgeGradle compile has NOT been run in this container because
its ForgeGradle/Minecraft dependency graph and Gradle wrapper are not available
locally and outbound dependency fetching is blocked. Do not rename this ZIP to
.jar or put it in the mods folder. The next milestone is compile -> launch ->
runtime fixes -> actual test JAR.

See PORT_STATUS_PHASE10.md and BUILDING.md for details.
