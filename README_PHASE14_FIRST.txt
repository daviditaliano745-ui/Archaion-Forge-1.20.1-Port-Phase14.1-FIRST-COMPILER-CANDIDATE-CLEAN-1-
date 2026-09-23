ARCHAION FORGE 1.20.1 PORT — PHASE 14
=====================================

This is the FIRST-COMPILER CANDIDATE source package, not the mods-folder JAR.

What changed from Phase 13:
- Fixed the bad SleepingState import in LastOfDeepslateTooltipRenderer.
- Added a one-button GitHub Actions Forge 47.4.20 build workflow.
- Added scripts/build_with_forge_mdk.sh for a local networked Java 17 build.
- The build bootstrap verifies the official Forge 47.4.20 MDK SHA-1 before running Gradle.

The next step is a real ForgeGradle compile. If it succeeds, use the reobfuscated JAR from build/libs/ for the first Minecraft launch test. If it fails, preserve the full compiler log; those errors are the next fix list.
