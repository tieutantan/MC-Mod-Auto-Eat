#!/bin/bash

# Script to run Minecraft client with NeoForge mod on macOS
# Automatically detects and sets Java 21 as the Gradle runtime

set -e

echo "[AutoEat] Checking for JDK 21..."

# Attempt to find Java 21 on macOS using /usr/libexec/java_home
JAVA_21_HOME=$(/usr/libexec/java_home -v 21 2>/dev/null || echo "")

if [ -z "$JAVA_21_HOME" ]; then
    echo "[AutoEat] ERROR: JDK 21 not found."
    echo "[AutoEat] This project requires Java 21 to build and run."
    echo ""
    echo "[AutoEat] Please install JDK 21 using one of the following methods:"
    echo "  - Homebrew: brew install openjdk@21"
    echo "  - SDKMAN!: sdk install java 21"
    echo "  - Oracle JDK: https://www.oracle.com/java/technologies/downloads/#java21"
    echo ""
    echo "[AutoEat] After installation, verify with: /usr/libexec/java_home -V"
    exit 1
fi

# Verify that the found Java is actually version 21, not a higher version
JAVA_VERSION=$("$JAVA_21_HOME/bin/java" -version 2>&1 | grep 'version' | head -n 1)
echo "[AutoEat] Java version check: $JAVA_VERSION"

if ! echo "$JAVA_VERSION" | grep -q '"21\.' && ! echo "$JAVA_VERSION" | grep -q '"21"'; then
    echo "[AutoEat] ERROR: Found Java at $JAVA_21_HOME, but it is not version 21."
    echo "[AutoEat] This is likely Java $(echo "$JAVA_VERSION" | grep -o '"[0-9]*' | tr -d '"'), which is incompatible."
    echo "[AutoEat] Please install JDK 21."
    echo ""
    echo "[AutoEat] Available java versions on this system:"
    /usr/libexec/java_home -V 2>&1 || true
    echo ""
    echo "[AutoEat] Install options:"
    echo "  - Homebrew: brew install openjdk@21"
    echo "  - SDKMAN!: sdk install java 21"
    echo "  - Oracle JDK: https://www.oracle.com/java/technologies/downloads/#java21"
    exit 1
fi

echo "[AutoEat] Found valid JDK 21 at: $JAVA_21_HOME"
echo "[AutoEat] Setting JAVA_HOME and stopping any existing Gradle daemons..."

# Stop existing Gradle daemons to ensure fresh startup with correct JVM
./gradlew --stop 2>/dev/null || true

# Set JAVA_HOME and run the client
echo "[AutoEat] Launching runClient..."
export JAVA_HOME="$JAVA_21_HOME"
exec ./gradlew runClient "$@"
