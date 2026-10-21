#!/bin/sh
# ----------------------------------------------------------------------------
# Student Budget Tracker Maven Wrapper Script for Unix / macOS / Linux
# ----------------------------------------------------------------------------

DIRNAME="$(cd "$(dirname "$0")" && pwd)"

if command -v mvn >/dev/null 2>&1; then
    exec mvn "$@"
fi

if [ -f "$DIRNAME/.mvn/wrapper/maven-wrapper.jar" ]; then
    exec java -jar "$DIRNAME/.mvn/wrapper/maven-wrapper.jar" "$@"
fi

echo "[ERROR] Maven could not be found. Please install Apache Maven." >&2
exit 1
