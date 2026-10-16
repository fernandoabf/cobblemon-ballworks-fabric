#!/usr/bin/env bash
# Runs Gradle 8.14.3 / JDK 21 inside Docker (image gradle:8.14.3-jdk21), as your uid:gid,
# with the Gradle cache (GRADLE_USER_HOME) and HOME outside the project.
# The project and the cache are mounted at the SAME absolute paths as on the host, so paths
# baked into Loom's caches/run configs stay valid on the host.
#
#   ./dgradle.sh build         -> compiles + build/libs/cobbleballworks-<ver>.jar (remapped, for the server/client)
#   ./dgradle.sh runGametest   -> headless gametest server, exits by itself (non-zero on failure)
#   ./dgradle.sh genSources    -> decompiled Minecraft 1.21.1 sources (Mojang mappings)
#
# Override the cache location with BALLWORKS_GRADLE_HOME=/some/dir ./dgradle.sh build
set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
GRADLE_HOME_DIR="${BALLWORKS_GRADLE_HOME:-${XDG_CACHE_HOME:-$HOME/.cache}/cobbleballworks-gradle}"
mkdir -p "$GRADLE_HOME_DIR"

exec docker run --rm -u "$(id -u):$(id -g)" \
    -e GRADLE_USER_HOME="$GRADLE_HOME_DIR" \
    -e HOME="$GRADLE_HOME_DIR" \
    -v "$PROJECT_DIR:$PROJECT_DIR" \
    -v "$GRADLE_HOME_DIR:$GRADLE_HOME_DIR" \
    -w "$PROJECT_DIR" \
    gradle:8.14.3-jdk21 \
    gradle --no-daemon --console=plain "$@"
