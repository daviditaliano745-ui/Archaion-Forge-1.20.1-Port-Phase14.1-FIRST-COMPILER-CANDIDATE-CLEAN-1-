# Building the Phase 13 Archaion 1.20.1 port

This project targets Minecraft 1.20.1 and Java 17. The ForgeGradle development dependency is pinned to Forge 47.4.20 to match the user's current pack, while mods.toml retains the Forge 47.2.30+ compatibility floor required by AAA Particles.

The source tree intentionally does not contain a fabricated Gradle wrapper. A real
Forge 1.20.1 MDK/Gradle wrapper and normal dependency resolution are required so
ForgeGradle can provide the Minecraft/Forge compile classpath and reobfuscation.

The user's exact AAA Particles 2.2.3 Forge 1.20.1 dependency is already included
under `libs/` and referenced by `build.gradle` through `fg.deobf(files(...))`.

With a normal Forge 1.20.1 Gradle environment available, the intended build task is:

    ./gradlew build

The reobfuscated mod JAR should then be produced under `build/libs/`.

Do not treat a plain `javac` compile without ForgeGradle as a valid Minecraft mod
build: Forge mappings, transformed dependencies, access rules, and reobfuscation
are part of the real build.
