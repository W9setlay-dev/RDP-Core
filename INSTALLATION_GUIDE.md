# RDP Core Installation & Integration Guide

**Version**: 1.0.0  
**Target**: Minecraft 1.12.2 Forge/Cleanroom  
**Date**: August 2026

---

## QUICK START

### Installation
1. Download `RDP-Core-1.0.0.jar` (from build output)
2. Place in: `mods/` directory of Minecraft instance
3. Ensure Minecraft 1.12.2 with Forge/Cleanroom is installed
4. Launch - RDP Core initializes automatically

### Verify Installation
- Watch server logs at startup for `[RDP] RDP Core initialized`
- Run `/rdp status` command in-game
- Expected output: Global RDP level, stage, active regions

---

## CONFIGURATION

### Config File Location
After first launch, auto-generated at:
```
config/rdpcore/rdpcore.conf
```

### Key Configuration Options

#### Simulation
- `SIMULATION_INTERVAL_TICKS`: How often simulation runs (default: 20 = 1 second)
- `ENABLE_ANOMALIES`: Spawn anomalies (true/false)
- `ENABLE_CHUNK_REWRITING`: Mutate terrain via Chunk Rewriter (true/false)

#### Stages
Each RDP stage has multipliers:
```
stage:
  RDPI:
    pressureMultiplier: 0.5
    anomalyMultiplier: 0.3
    mutationMultiplier: 0.2
```
Higher multipliers = faster escalation at that stage.

#### Regions
- `REGION_SIZE_CHUNKS`: Size of RDP regions (16 = 256×256 blocks)
- `MAX_REGIONS_ACTIVE`: Prevent memory overflow (default: 500)
- `REGION_CACHE_MODE`: LRU player-prioritized or fixed

#### Mutations
- `CHUNK_REWRITE_THRESHOLD`: RDP level to trigger mutations (default: 0.4)
- `CHUNK_REWRITE_BUDGET_PER_TICK`: Max mutations/tick (default: 5)
- `MUTATION_NORMAL_BUDGET`: Chunks per request (default: 100)

#### Integrations
- `ENABLE_SCP_INTEGRATION`: true/false
- `ENABLE_SRP_INTEGRATION`: true/false
- `ENABLE_GAMESTAGES_INTEGRATION`: true/false

---

## CHUNK REWRITER INTEGRATION

### Required for World Mutation
RDP Core can queue mutation requests to Chunk Rewriter for terrain changes.

### Install Chunk Rewriter
1. Download `RDP-ChunkRewriter-1.0.0.jar`
2. Place in same `mods/` directory as RDP Core
3. Restart server

### Verify Integration
- At startup, logs should show: `[RDP] Chunk Rewriter: CONNECTED`
- If missing, logs show: `[RDP] Chunk Rewriter: NOT FOUND` (non-blocking)
- Run `/rdp status` - check "Chunk Rewriter Status"

### Without Chunk Rewriter
RDP Core continues operating normally:
- Simulation still advances RDP
- Regional pressure still calculates
- Anomalies still spawn
- **No terrain mutations** (queued but not executed)

---

## MOD INTEGRATIONS

### SCP-001 / SCP Project Anomalous
**Status**: Optional  
**Effect**: SCP entities increase pressure, RDP stages trigger SCP events  

Install SCP Project Anomalous mod alongside RDP Core.

Verify: `/rdp status` shows "SCP: CONNECTED" or "SCP: NOT FOUND"

### SRP (Scape and Run: Parasites)
**Status**: Optional  
**Effect**: Parasite entities count toward regional pressure  

Install SRP mod alongside RDP Core.

Verify: `/rdp status` shows "SRP: CONNECTED" or "SRP: NOT FOUND"

### GameStages
**Status**: Optional  
**Effect**: Player progression gated by RDP stages  

Install GameStages alongside RDP Core.

Verify: `/rdp status` shows "GameStages: CONNECTED" or "GameStages: NOT FOUND"

---

## COMMANDS

### `/rdp status`
**Permissions**: OP only  
**Output**: Global state, stages, regions, integration status

```
[RDP Status]
Global RDP: 0.35
Stage: RDPI
Active Regions: 12
Chunk Rewriter: CONNECTED
SRP: CONNECTED
SCP: NOT FOUND
GameStages: CONNECTED
```

### `/rdp telemetry`
**Permissions**: OP only  
**Output**: Performance metrics, mutation statistics

```
[RDP Telemetry]
Last Simulation: 2.3ms
Regions Processed: 12
Pressure Collection: 0.8ms
Anomalies Updated: 1.2ms
Mutations Queued: 3
Mutations Accepted: 2
```

### `/rdp debug`
**Permissions**: OP only  
**Output**: Detailed regional state, anomalies, hotspots

### `/rdp simulate`
**Permissions**: OP only  
**Effect**: Force one simulation cycle (useful for testing)

### `/rdp set <param> <value>`
**Permissions**: OP only  
**Effect**: Adjust RDP level or configuration at runtime

```
/rdp set global_rdp 0.5          # Set global RDP to 0.5
/rdp set enable_mutations true   # Enable mutations
```

---

## WORLD SAVE COMPATIBILITY

### Fresh World (No Prior RDP Data)
- RDP Core initializes with default state on first load
- Global RDP starts at 0.0
- No regions initially (created on-demand)
- Safe to add RDP Core to existing world

### Existing RDP World
- RDPWorldState loads from NBT
- All regions, anomalies, scars, hotspots restored
- Simulation resumes from saved state
- Persistence is reliable across save/load cycles

### Removing RDP Core
- World data left intact (safe to remove)
- Future RDP re-installation will restore saved state
- No world corruption

---

## TROUBLESHOOTING

### Server won't start
**Check logs** for:
- `[RDP] RDP Core initialized` - Module loaded successfully
- `[ERR]` messages - Specific initialization failures

**Common issues**:
- Missing dependencies? (Nothing required, all optional)
- Java version mismatch? (Should be 1.8+)
- Corrupted config? Delete `config/rdpcore/` and regenerate

### Chunk Rewriter shows "NOT FOUND"
- Chunk Rewriter jar not in mods/ directory
- Correct jar name? Should be `RDP-ChunkRewriter-*.jar`
- Check logs for: `WARN: RDP Chunk Rewriter not found in classpath`

**Fix**: Install Chunk Rewriter or disable mutations in config

### RDP not increasing
**Check**:
- Is simulation enabled? (`ENABLE_ANOMALIES: true`)
- Is simulation interval reasonable? (Default 20 ticks)
- Are players active in world? (Pressure needs players)
- Check `/rdp telemetry` - Simulation running?

**Debug**:
- Run `/rdp simulate` to force one cycle
- Check logs for `Running RDP simulation for world`

### Performance lag
**Diagnosis**:
- Check `/rdp telemetry` - Simulation time >5ms?
- How many active regions? (Check `/rdp status`)
- How many mutations queued? (Check telemetry)

**Solutions**:
- Increase `SIMULATION_INTERVAL_TICKS` in config (run less often)
- Decrease `CHUNK_REWRITE_BUDGET_PER_TICK` (process fewer mutations/tick)
- Decrease `MAX_REGIONS_ACTIVE` (focus simulation on fewer regions)

### Mutations not executing
**Verify**:
- Chunk Rewriter installed? (Check `/rdp status`)
- Is `ENABLE_CHUNK_REWRITING` true in config?
- Is regional RDP ≥ `CHUNK_REWRITE_THRESHOLD`?

**Debug**:
- Run `/rdp set global_rdp 0.5` to force high RDP
- Check if mutations appear in `/rdp telemetry`
- Check Chunk Rewriter logs for rejection reasons

### World save taking too long
**Cause**: RDP state serialization to NBT

**Solutions**:
- Increase `RDP_SAVE_INTERVAL_TICKS` (save less frequently)
- Reduce number of scars/anomalies (age them out)
- Monitor with `/rdp telemetry`

### Mods not integrating
**Check logs for**:
- `[OK]` - Successfully integrated
- `[SKIP]` - Mod not detected (not installed)
- `[ERR]` - Integration failed (check error message)

**Example**:
```
Initializing modpack integrations...
  [OK] SRP integration initialized
  [SKIP] SCP-001 not detected
  [OK] GameStages integration initialized
```

---

## ADVANCED CONFIGURATION

### Per-Stage Escalation
Customize how quickly RDP escalates at each stage:

```yaml
stages:
  RDP0:
    pressureMultiplier: 0.1
    anomalyMultiplier: 0.05
    mutationMultiplier: 0.01
  RDPI:
    pressureMultiplier: 0.5
    anomalyMultiplier: 0.3
    mutationMultiplier: 0.2
  RDPII:
    pressureMultiplier: 1.0
    anomalyMultiplier: 0.6
    mutationMultiplier: 0.5
  # ... etc
```

Lower multipliers = slower progression at that stage  
Higher multipliers = faster escalation

### Custom Mutation Profiles
Define mutation intensity profiles for Chunk Rewriter:

```yaml
mutation_profiles:
  rdp_regional_growth:
    base_intensity: 0.5
    max_chunks: 100
  rdp_catastrophic:
    base_intensity: 0.9
    max_chunks: 500
```

### Dimension-Specific Rules
Configure RDP behavior per dimension:

```yaml
dimensions:
  "0":  # Overworld
    enable_mutations: true
    pressure_multiplier: 1.0
  "1":  # Nether
    enable_mutations: false
    pressure_multiplier: 0.5
  "-1":  # End
    enable_mutations: false
    pressure_multiplier: 0.2
```

---

## PERFORMANCE TUNING

### High-Load Servers
```
SIMULATION_INTERVAL_TICKS: 40          # Run less frequently
CHUNK_REWRITE_BUDGET_PER_TICK: 2       # Fewer mutations per tick
MAX_REGIONS_ACTIVE: 250                # Smaller active region pool
REGION_PRESSURE_DECAY: 0.01            # Faster decay (less escalation)
ENABLE_CHUNK_REWRITING: false          # Disable mutations if needed
```

### Low-Load Servers / Single Player
```
SIMULATION_INTERVAL_TICKS: 10          # Run more frequently
CHUNK_REWRITE_BUDGET_PER_TICK: 10      # More mutations per tick
MAX_REGIONS_ACTIVE: 500                # Use more regions
REGION_PRESSURE_DECAY: 0.005           # Slower decay (more escalation)
ENABLE_CHUNK_REWRITING: true           # Enable mutations
```

### Balanced (Default)
```
SIMULATION_INTERVAL_TICKS: 20
CHUNK_REWRITE_BUDGET_PER_TICK: 5
MAX_REGIONS_ACTIVE: 300
REGION_PRESSURE_DECAY: 0.008
ENABLE_CHUNK_REWRITING: true
```

---

## SUPPORT

### Report Issues
1. Check `/rdp status` and `/rdp telemetry` output
2. Review server logs for `[RDP]` entries
3. Test with `/rdp simulate` to verify simulation works
4. Check configuration in `config/rdpcore/`

### Debug Logging
Enable debug output in config:
```
logging_level: DEBUG
```
More verbose logs will appear in server console.

### Disable RDP Core
To test if RDP Core is causing issues:
1. Remove JAR from mods/ directory
2. Restart server
3. If lag/issues disappear, RDP Core was related

---

## VERSION COMPATIBILITY

**Supports**:
- Minecraft 1.12.2 (Forge, Cleanroom)
- Java 8+ (development: Java 17+)
- RDP Chunk Rewriter 1.0.0+ (optional)
- SRP, SCP Project Anomalous, GameStages (optional)

**Does Not Support**:
- Minecraft 1.11.2 or earlier
- Minecraft 1.13+ (too many breaking changes)
- Forge versions <14.x

---

**Last Updated**: August 2026  
**Tested On**: Minecraft 1.12.2, Cleanroom Loader, Java 21
