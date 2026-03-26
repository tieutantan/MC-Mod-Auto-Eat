
# AutoEat (NeoForge 1.21.1)

AutoEat is an automatic eating mod for Minecraft 1.21.1 (NeoForge). It does not require holding food in hand and does not require right-clicking.

## Features

- If health is at or below 85% and the player is still hungry, auto-eat stays active until hunger is full.
- If health is above 85%, auto-eat only triggers while hunger is below 70% (< 14/20).
- Eats exactly 1 item every 80 ticks whenever either condition above is active.
- Prioritizes inventory order from top to bottom (lower slot index first).
- Correctly decreases consumed food item count.
- After each eat action, sends 1 chat line with remaining count in the consumed stack + one random message from `src/main/resources/autoeat_messages.txt` (500 messages).

## How It Works

- The mod runs on server tick (`PlayerTickEvent.Post`).
- Every 80 ticks, the mod checks the player's health first.
- If health is at or below 85%, it consumes 1 food item whenever the hunger bar is not yet full.
- If health is above 85%, it consumes 1 food item only while hunger is below 70%.
- Chat messages are loaded once at class initialization and cached in memory (no file reads on each eat).
- If the message file is missing or empty, the mod falls back to a default message to avoid crashes.

## Installation

1. Install NeoForge for Minecraft `1.21.1`.

2. **Install JDK 21** (required for building this mod):
   - **Homebrew** (macOS): `brew install openjdk@21`
   - **SDKMAN!**: `sdk install java 21`
   - **Oracle JDK**: [Download from Oracle](https://www.oracle.com/java/technologies/downloads/#java21)
   - **Other package managers**: See https://adoptium.net/

3. Verify JDK 21 is installed:
   ```bash
   /usr/libexec/java_home -V
   ```
   You should see `21` in the list.

4. Build mod:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
./gradlew build
```

5. Get file jar in `build/libs/`.
6. Copy the generated JAR into your Minecraft `mods` folder (NeoForge 1.21.1).

## Development Run

**Ensure JDK 21 is installed first** (see Installation section above).

```bash
./run-client.sh
```

The `run-client.sh` script automatically detects JDK 21 on your system and launches the client with the correct settings.
If JDK 21 is not found or a different Java version is detected, the script will show an error with installation instructions.

## Configuration

The current version does not use a config file. All behavior is fixed and automatic by default.

## Relevant Files

- Auto-eat logic: `src/main/java/com/tantn/autoeat/AutoEatEvents.java`
- Random messages: `src/main/resources/autoeat_messages.txt`

## Notes

- Some template example assets (example block/item) may produce model warnings in dev runs, but they do not affect AutoEat features.
