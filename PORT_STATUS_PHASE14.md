# Archaion Forge 1.20.1 Port — Phase 14 First-Compiler Candidate

Phase 14 is the source tree intended for the first real ForgeGradle compile.

## Changes since Phase 13

- Fixed a genuine compile blocker in `LastOfDeepslateTooltipRenderer`: `SleepingState` now imports from `com.ratrod.archaion.entities.SleepingState`, the package where the enum actually exists.
- Added `.github/workflows/build-forge.yml` for a reproducible Java 17 / Forge 47.4.20 build on a networked GitHub runner.
- Added `scripts/build_with_forge_mdk.sh` for the same build locally on any normal networked Linux/macOS environment.
- Both bootstrap paths download the official Forge 1.20.1-47.4.20 MDK and verify its published SHA-1 (`ae89b7adec05802fb805c9345b509029c6952ebb`) before using its Gradle wrapper.
- Bumped the test build version to `1.20.1-1.4.3-port.test6`.

## Important status

This package is **not** a compiled mods-folder JAR. The current ChatGPT execution sandbox cannot make outbound HTTPS connections, so ForgeGradle and Minecraft dependencies cannot be resolved here.

The next authoritative step is `clean build` on a networked Java 17 runner. Any failure from that build should now be treated as a concrete compiler/API issue and fixed from its log; no further broad feature-port phase is planned before that compile.
