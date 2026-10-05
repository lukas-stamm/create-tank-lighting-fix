# Create Tank Lighting Fix

Fixes Create fluid tanks losing **block light** after a chunk unload/reload.

After leaving an area and coming back, glowing fluids (lava, etc.) still look bright in the tank, but light no longer spreads into the room. Using a Create wrench on the tank fixed it — this mod does that refresh automatically.

## Requirements

| | Version |
|---|---|
| Minecraft | 1.21.1 |
| NeoForge | 21.1.248+ |
| Create | 6.0.10 (up to but not including 6.1.0) |

## Installation

1. Install NeoForge for 1.21.1.
2. Install [Create](https://modrinth.com/mod/create).
3. Drop this mod’s jar into your `mods` folder.

## Building

```bash
./gradlew build
```

The jar is written to `build/libs/`.

JDK 21 is required.

## How it works

Create stores tank luminosity on the block entity, but after reload the **server** light engine often keeps a stale value of 0. The fluid renderer still looks correct, so the tank appears to glow without lighting the surroundings.

This mod:

1. Re-applies Create’s `setWindows` path when a player watches/receives a chunk (same server light refresh as the wrench).
2. Calls `checkBlock` on the server when luminosity changes, so newly filled tanks save correct light.

## License

MIT
