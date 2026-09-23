# Archaion Forge 1.20.1 — Phase 14.1 Compiler Candidate Cleanup

Phase 14.1 is the cleaned first-compiler candidate.

Changes from Phase 14:
- Removed stray javac argument scratch file.
- Added .gitignore rules for Gradle/build/run/scratch output.
- Revalidated AAA Particles dependency metadata: mod id `aaa_particles`, version 2.2.3, Forge 47.2.30+.
- Re-ran JSON/internal-import/build-script preflight.

Validation:
- 110 Java sources
- 158 JSON/pack JSON files, 0 parse errors
- 20 NBT structure files present
- 0 broken internal Archaion imports
- 0 javac scratch files
- GitHub Forge 47.4.20 build workflow present
- local Forge MDK bootstrap script passes `bash -n`

The remaining gate is a real ForgeGradle 47.4.20 / Java 17 compile on a networked runner.
