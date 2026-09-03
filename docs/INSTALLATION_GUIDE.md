# RDP Core Installation Guide

## Prerequisites

- Minecraft 1.12.2
- Forge 1.12.2-14.23.5.2860 or compatible
- Java 8+
- The R.D.P. modpack with dependent mods

## Installation Steps

### 1. Build the Mod

```bash
cd D:\Forge Modding\RDP Core
gradlew.bat build
```

This will generate `build/libs/rdpcore-1.0.0-dev.jar`

### 2. Add to Modpack

Copy the compiled JAR to your modpack's `mods/` directory:

```
D:\ElyPrismLauncher\instances\RDP - (Reality Distortion Phenomen-v1)\minecraft\mods\rdpcore-1.0.0-dev.jar
```

### 3. Verify Dependencies

Ensure these mods are present in your modpack:
- RDP Chunk Rewriter (chunk mutation engine)
- SCP-001 Controller (optional but recommended)
- Scape and Run: Parasites (optional for SRP integration)
- GameStages (optional for progression gating)
- InControl (optional for mob rule customization)
- BiomeTweaker (optional for biome mutations)

### 4. Configure RDP Core

Create/edit `config/rdpcore.cfg`:

```ini
# Global RDP progression
global_rdp_increment_per_tick=0.00001
enable_rdp_progression=true

# Regional settings
region_size_chunks=16
region_pressure_decay=0.001

# Chunk rewriter
enable_chunk_rewriting=true
chunk_rewrite_threshold=0.25

# Integrations
enable_scp001_integration=true
enable_chunk_rewriting=true
```

### 5. Launch Minecraft

Start your modpack. RDP Core will initialize on world load.

## First Run

When you first load a world:

1. RDP Core will create global state tracking
2. Per-region data will be generated as chunks load
3. Global RDP will begin incrementing (configurable)
4. Log output will show initialization status:

```
[rdpcore] R.D.P. CORE - REALITY DISTORTION PHENOMENON
[rdpcore] Loading fundamental world-state simulation system...
[rdpcore] Configuration loaded.
[rdpcore] Core data structures initialized.
[rdpcore] Registering event handlers...
[rdpcore] Event handlers registered and simulation engine initialized.
[rdpcore] Post-initialization: performing integrations...
[rdpcore] SCP-001 Controller detected. Initializing integration...
[rdpcore] SCP-001 integration initialized.
[rdpcore] Post-initialization complete. R.D.P. Core ready.
```

## Existing World Compatibility

RDP Core is designed to be safe to add to existing worlds:

1. World data is preserved
2. Chunk state is created on first load
3. Global RDP starts at 0.0 (configurable via config)
4. No vanilla blocks are destroyed on initial load

To start an existing world with RDP at a specific level, edit the config before first load, then use the admin command:

```
/rdp setglobal 0.25
```

## Troubleshooting

### "Gradle requires JVM 17 or later"

Use Java 17+ for compilation:

```bash
set JAVA_HOME=C:\Program Files\Java\jdk-17
gradlew.bat build
```

### "RDP Core fails to load"

Check the log file for errors:

```
D:\ElyPrismLauncher\instances\RDP - (Reality Distortion Phenomen-v1)\minecraft\logs\latest.log
```

### "Integrations not working"

Verify that dependent mods are installed:

```
/modlist
```

If a mod is missing, RDP Core will automatically disable that integration.

### "No chunk mutations occurring"

Check:
1. Global RDP >= 0.25 (view with `/rdp info`)
2. Chunk Rewriter is enabled in config
3. Check server logs for mutation errors

### "Performance issues"

Reduce the mutation budget:

```ini
chunk_rewrite_budget_per_tick=2
```

Or disable problematic integrations in the config.

## Commands

### For Players

```
/rdp info                 # Show current RDP level and stage
/rdp help                 # Show all commands
```

### For Administrators

```
/rdp setglobal <level>    # Set global RDP level (0.0-1.0)
/rdp region <x> <z>       # Show regional RDP data
/rdp stage                 # Show current RDP stage
/rdp pressure <x> <z>     # Show regional pressure
/rdp reload               # Reload configuration
```

## Performance Considerations

- RDP Core runs mostly on the server tick
- Regional updates are distributed across ticks
- Chunk mutations are budget-limited (configurable)
- Most operations are cached to avoid recomputation

On large modpacks, expected performance impact:

- Global RDP progression: < 0.1ms/tick
- Regional updates: 0.1-0.5ms/tick (depends on regions loaded)
- Mutation processing: 1-5ms/tick (depends on budget)

Total: Usually 1-6ms/tick, well within the 50ms server tick budget.

## Multiplayer Considerations

- RDP Core state is stored per-world
- All players in the same world see the same global RDP level
- Regional anomalies are synchronized to clients
- Reality anchors are visible to all players

For multiplayer servers:

1. Global RDP is shared across all players
2. Disable player commands if desired:

```ini
enable_player_commands=false
```

## Uninstall

Simply remove the JAR file and delete `config/rdpcore.cfg`.

World data will be preserved (RDP data will simply not be updated).

---

**Version**: 1.0  
**Last Updated**: 2026-08-27
