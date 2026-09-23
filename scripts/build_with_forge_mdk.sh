#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

MDK_URL="https://maven.minecraftforge.net/net/minecraftforge/forge/1.20.1-47.4.20/forge-1.20.1-47.4.20-mdk.zip"
MDK_SHA1="ae89b7adec05802fb805c9345b509029c6952ebb"

if [[ ! -x ./gradlew || ! -f gradle/wrapper/gradle-wrapper.jar ]]; then
  command -v curl >/dev/null || { echo "curl is required" >&2; exit 2; }
  command -v unzip >/dev/null || { echo "unzip is required" >&2; exit 2; }
  command -v sha1sum >/dev/null || { echo "sha1sum is required" >&2; exit 2; }

  tmp="$(mktemp -d)"
  trap 'rm -rf "$tmp"' EXIT
  curl --fail --location --retry 3 --output "$tmp/forge-mdk.zip" "$MDK_URL"
  echo "$MDK_SHA1  $tmp/forge-mdk.zip" | sha1sum --check --strict
  unzip -q "$tmp/forge-mdk.zip" -d "$tmp/mdk"
  rm -rf gradle
  cp -a "$tmp/mdk/gradle" ./gradle
  cp "$tmp/mdk/gradlew" ./gradlew
  cp "$tmp/mdk/gradlew.bat" ./gradlew.bat
  chmod +x ./gradlew
fi

exec ./gradlew --no-daemon --stacktrace clean build
