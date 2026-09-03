# R.D.P. Core - Architecture and Integration Plan

## Executive Summary

R.D.P. (Reality Distortion Phenomenon) Core is a world-level simulation framework for Minecraft 1.12.2 that unifies major modpack systems into one coherent cosmological-horror phenomenon. This document outlines the architecture and integration strategy based on analysis of existing projects.

## 1. Project Inventory

### 1.1 Existing RDP Chunk Rewriter
- **Location**: `D:\Forge Modding\RDP Chunk Rewriter\RDPChunkRewriter`
- **Status**: Production-ready mutation engine
- **Key Features**:
  - Queue-based chunk processing
  - Per-chunk NBT persistence
  - Staged rewrite pipeline (TERRAIN -> BLOCKS -> STRUCTURES -> ANOMALIES -> COMPLETE)
  - MutationRequest API for external mutation requests
  - Region-based batch mutations
  - Budget-based execution (controlled tick load)

### 1.2 SCP-001 Controller
- **Location**: `D:\ElyPrismLauncher\instances\RDP - (Reality Distortion Phenomen-v1)\minecraft\scp001controller`
- **Status**: Custom mod for SCP-001 manifestations
- **Purpose**: Anomalous entity spawning and reality warping events

### 1.3 Minecraft Instance / Modpack
- **Location**: `D:\ElyPrismLauncher\instances\RDP - (Reality Distortion Phenomen-v1)\minecraft`
- **Key Mods** (to be determined by inspection):
  - Scape and Run: Parasites (SRP) - biological evolution system
  - GameStages - progression gating
  - InControl - mob rule customization
  - BiomeTweaker - biome mutations
  - CraftTweaker - recipe adjustments
  - Recurrent Complex - structure generation
  - And many others...

## 2. Core Architecture

### 2.1 Design Philosophy

RDP Core unifies the modpack through a single cosmological-horror framework:

```
                          R.D.P. CORE
                               ¦
           --------------------+-------------------¬
           ¦                   ¦                   ¦
        BIOLOGY            GEOGRAPHY            PHYSICS
           ¦                   ¦                   ¦
          SRP             BIOME MUTATION       BLOCK REWRITE
           ¦                   ¦                   ¦
           L-------------------+--------------------
                               ¦
                          ANOMALIES
                               ¦
              -----------------+----------------¬
              ¦                ¦                ¦
           SPATIAL          TEMPORAL       DIMENSIONAL
              ¦                ¦                ¦
              L----------------+-----------------
                               ¦
                         COSMOLOGICAL
                               ¦
                            RDP-X
```

### 2.2 Global RDP Level

The **Global RDP Level** represents world-wide progression (0.0 to 1.0):

```
0.00 - 0.09  -> RDP-0   (Normal Minecraft, small anomalies)
0.10 - 0.24  -> RDP-I   (First distortions appear)
0.25 - 0.39  -> RDP-II  (Regional instability, biome anomalies)
0.40 - 0.54  -> RDP-III (Persistent mutations, hotspots)
0.55 - 0.69  -> RDP-IV  (Reality instability, temporal effects)
0.70 - 0.79  -> RDP-V   (Dimensional leakage, major SCP)
0.80 - 0.89  -> RDP-VI  (Spatial collapse, sky anomalies)
0.90 - 0.96  -> RDP-VII (Cosmological instability)
0.97 - 1.00  -> RDP-X   (JUDGEMENT DAY - Reality breakdown)
```

### 2.3 Local RDP Regions

Every 16x16 chunk region (256x256 blocks) tracks:
- **Local RDP Level**: Regional distortion severity
- **Pressure**: Accumulated distortion force from:
  - SRP parasite evolution rate
  - Anomalous entity activity
  - SCP manifestation presence
  - Chunk rewrite intensity
  - Destroyed reality anchors
  - Existing RDP scars
  - Player interaction
  
- **Anomalies**: Spatial, temporal, dimensional distortions
- **Reality Anchors**: Resist RDP progression locally
- **Scars**: Permanent marks of reality instability

### 2.4 Integration Layer

RDP Core acts as a command/event bus that:
1. Reads global/local RDP levels
2. Calculates required mutations and anomalies
3. Issues mutation requests to Chunk Rewriter
4. Triggers SCP-001 controller events
5. Adjusts SRP evolution parameters
6. Adjusts mob rules via InControl
7. Modifies biome properties via BiomeTweaker
8. Applies GameStages progression gates

## 3. Module Architecture

### 3.1 Core Modules

```
net.vas.rdpcore/
+-- core/                    # Global RDP level tracking
¦   +-- GlobalRDPLevel.java
¦   L-- RDPStage.java
¦
+-- region/                  # Regional RDP state management
¦   +-- RDPRegion.java
¦   +-- RDPRegionManager.java
¦   L-- RegionStorage.java
¦
+-- world/                   # World-level state persistence
¦   +-- RDPWorldState.java
¦   L-- RDPWorldData.java
¦
+-- simulation/              # Simulation engine
¦   +-- RDPSimulationEngine.java
¦   +-- SimulationTicker.java
¦   L-- EventQueue.java
¦
+-- anomaly/                 # Anomaly systems
¦   +-- AnomalyRegistry.java
¦   +-- spatial/
¦   ¦   +-- SpatialAnomaly.java
¦   ¦   L-- SpatialAnomalyGenerator.java
¦   +-- temporal/
¦   ¦   +-- TemporalAnomaly.java
¦   ¦   L-- TemporalAnomalyGenerator.java
¦   L-- dimensional/
¦       +-- DimensionalAnomaly.java
¦       L-- DimensionalAnomalyGenerator.java
¦
+-- entity/                  # Reality anchor system
¦   +-- RealityAnchor.java
¦   +-- RealityAnchorEntity.java
¦   L-- AnchorRegistry.java
¦
+-- mutation/                # Mutation request system
¦   +-- MutationCoordinator.java
¦   L-- ChunkRewriterAdapter.java
¦
+-- integration/             # Modpack integrations
¦   +-- scp/
¦   ¦   L-- SCP001Integration.java
¦   L-- modpack/
¦       +-- SRPIntegration.java
¦       +-- GameStagesIntegration.java
¦       +-- InControlIntegration.java
¦       L-- BiomeTweakerIntegration.java
¦
+-- rendering/              # Client-side rendering
¦   +-- SkySkyRenderer.java
¦   +-- AnomalyRenderer.java
¦   L-- HUDRenderer.java
¦
+-- config/                 # Configuration
¦   +-- RDPConfig.java
¦   L-- ConfigValues.java
¦
+-- network/                # Network synchronization
¦   +-- RDPPacketHandler.java
¦   L-- packets/
¦       +-- GlobalRDPUpdatePacket.java
¦       L-- RegionalUpdatePacket.java
¦
+-- command/                # Debug commands
¦   +-- RDPCommand.java
¦   +-- SetRDPCommand.java
¦   L-- RegionDebugCommand.java
¦
+-- util/                   # Utilities
¦   +-- RDPMath.java
¦   +-- RegionCoordUtils.java
¦   L-- NBTUtils.java
¦
+-- api/                    # Public API
¦   L-- RDPAPI.java
¦
L-- RDPCore.java           # Main mod entry point
```

## 4. Integration Strategy

### 4.1 RDP Chunk Rewriter Integration

**Approach**: Use the existing MutationRequest API

```java
MutationRequest request = MutationRequest.builder()
    .world(world)
    .center(chunkX, chunkZ)
    .radius(8)
    .profile("rdp_stage_5")
    .intensity(0.72F)
    .priority(100)
    .budget(500)
    .cause("RDP_REGIONAL_COLLAPSE")
    .build();

MutationResult result = RDPChunkRewriter.getMutationEngine()
    .requestRegionMutation(request);
```

**Trigger Conditions**:
- Global RDP >= 0.25 (RDP-II) - begin small mutations
- Regional pressure > threshold - localized mutations
- Temporal anomaly spawns - concentrated mutations
- Chunk degradation score > 0.7 - intensive mutations

### 4.2 SCP-001 Controller Integration

**Approach**: Listen for RDP stage changes, emit SCP events

**Events to Trigger**:
- RDP-II reached: First SCP manifestations
- RDP-IV reached: Major SCP activity
- RDP-V reached: Dimensional leakage (new dimensions open)
- RDP-VI reached: Reality distortion becomes visible
- RDP-X reached: Full SCP-001 containment failure

### 4.3 Scape and Run: Parasites (SRP) Integration

**Approach**: Adjust SRP evolution parameters based on RDP

**Mechanism**:
- SRP infection spreads faster at higher RDP
- SRP mutations become more exotic at higher RDP
- SRP parasite population affected by RDP pressure
- Reverse: Heavy SRP activity increases regional pressure

### 4.4 GameStages Integration

**Approach**: Lock/unlock stages based on RDP progression

**Stages**:
- Stage "RDP_I": Unlocked at RDP >= 0.10
- Stage "RDP_II": Unlocked at RDP >= 0.25
- ... (one per RDP stage)
- Stage "JUDGEMENT_DAY": Unlocked at RDP >= 0.97

### 4.5 InControl Integration

**Approach**: Modify mob rules based on RDP level

**Examples**:
- At RDP-III: Anomalous mobs spawn in distorted areas
- At RDP-IV: Temporal anomalies cause strange mob behavior
- At RDP-VI: Mobs behave erratically in sky anomaly zones

### 4.6 BiomeTweaker Integration

**Approach**: Apply biome mutations to affected regions

**Mutations**:
- Temperature shifts (thermal anomalies)
- Rainfall changes (dimensional weather)
- Biome color shifts (visual distortion)
- New block generation (reality degradation)

## 5. Event Flow

### 5.1 Per-Tick Simulation Loop

```
ServerTickEvent.START
  ¦
  +-> RDPSimulationEngine.onServerTick()
  ¦    +-> Update global RDP level (if enabled)
  ¦    +-> For each loaded region:
  ¦    ¦   +-> Decay pressure
  ¦    ¦   +-> Update local RDP based on pressure
  ¦    ¦   +-> Age anomalies (remove expired)
  ¦    ¦   L-> Check for new anomaly spawn conditions
  ¦    ¦
  ¦    +-> Process mutation request queue
  ¦    ¦   +-> Get pending requests
  ¦    ¦   +-> Issue to ChunkRewriter with budget
  ¦    ¦   L-> Track completion status
  ¦    ¦
  ¦    +-> Check for stage transitions
  ¦    ¦   L-> Fire stage events to all integrations
  ¦    ¦
  ¦    L-> Persist changed state (every 60 seconds)
  ¦
ServerTickEvent.END
```

### 5.2 Chunk Load Event

```
ChunkEvent.Load
  ¦
  +-> RDPChunkEventHandler.onChunkLoad()
  ¦    +-> Load region if not in memory
  ¦    +-> Check if chunk needs mutation
  ¦    +-> Calculate local RDP for this chunk
  ¦    L-> Queue for potential rewriting
  ¦
ChunkDataEvent.Load
  ¦
  +-> RDPChunkStorage.onChunkDataLoad()
  ¦    L-> Restore per-chunk RDP state from NBT
  ¦
ChunkDataEvent.Save
  ¦
  +-> RDPChunkStorage.onChunkDataSave()
  ¦    L-> Persist per-chunk RDP state to NBT
```

### 5.3 World Save

```
WorldEvent.Save
  ¦
  +-> RDPWorldStorage.onWorldSave()
  ¦    +-> Serialize global RDP level
  ¦    +-> Serialize all region states
  ¦    +-> Write to world NBT
  ¦    L-> Update timestamps
  ¦
WorldEvent.Load
  ¦
  +-> RDPWorldStorage.onWorldLoad()
  ¦    +-> Deserialize global RDP level
  ¦    +-> Load regions on-demand
  ¦    L-> Validate state integrity
```

## 6. Persistent Storage Strategy

### 6.1 Per-Chunk Storage (Via Chunk NBT)

```nbt
{
  "RDPData": {
    "version": 1,
    "chunkRDP": 0.42D,
    "degradationScore": 0.65D,
    "anomalies": [
      {
        "type": "TEMPORAL",
        "intensity": 0.8D,
        "age": 150,
        "x": 5, "y": 64, "z": 8
      }
    ],
    "rewriteHistory": [
      {
        "timestamp": 1234567890000L,
        "cause": "RDP_PRESSURE_BUILDUP",
        "stage": "BLOCKS"
      }
    ],
    "scars": 3
  }
}
```

### 6.2 Region Storage (Via World NBT)

```nbt
{
  "RDPRegions": {
    "[16,16]": {
      "localRDPLevel": 0.35D,
      "pressure": 0.42D,
      "realityAnchorCount": 2,
      "scarCount": 1,
      "anomalies": [ ... ],
      "eventHistory": { ... }
    },
    "[17,16]": { ... }
  },
  "GlobalRDP": {
    "level": 0.28D,
    "stage": "RDP-II",
    "lastUpdateTick": 1234567890000L,
    "judgementDayActive": false
  }
}
```

## 7. Configuration System

All settings can be controlled via `config/rdpcore.cfg`:

```ini
# Global Progression
global_rdp_increment_per_tick=0.00001
enable_rdp_progression=true
rdp_save_interval_ticks=1200

# Regional Settings
region_size_chunks=16
region_pressure_decay=0.001
max_anomalies_per_region=10

# Chunk Rewriter Integration
enable_chunk_rewriting=true
chunk_rewrite_threshold=0.25
chunk_rewrite_budget_per_tick=5

# SCP-001 Integration
enable_scp001_integration=true
scp001_rdp_contribution=1.0

# Anomalies
enable_temporal_anomalies=true
enable_spatial_anomalies=true
enable_dimensional_anomalies=true
temporal_anomaly_threshold=0.55
spatial_anomaly_threshold=0.40
dimensional_anomaly_threshold=0.70

# Reality Anchors
enable_reality_anchors=true
reality_anchor_rdp_resistance=0.05

# Judgement Day
enable_judgement_day=true
judgement_day_threshold=0.97
```

## 8. Development Roadmap

### Phase 1: Core Framework (CURRENT)
- [x] Project setup and structure
- [x] Global RDP level tracking
- [x] Regional state management  
- [x] World persistence layer
- [x] Configuration system
- [x] Event registration
- [ ] Core simulation engine

### Phase 2: Simulation Engine
- [ ] Per-tick progression logic
- [ ] Regional pressure calculations
- [ ] Anomaly generation and aging
- [ ] Stage transition detection
- [ ] Event queuing system

### Phase 3: Chunk Rewriter Integration
- [ ] MutationRequest adapter
- [ ] Budget-aware queuing
- [ ] Mutation effect mapping
- [ ] Testing and validation

### Phase 4: Modpack Integrations
- [ ] SCP-001 Controller
- [ ] Scape and Run: Parasites
- [ ] GameStages
- [ ] InControl
- [ ] BiomeTweaker

### Phase 5: Anomaly Systems
- [ ] Spatial anomalies
- [ ] Temporal anomalies
- [ ] Dimensional anomalies
- [ ] Reality anchors

### Phase 6: Client-Side Rendering
- [ ] Sky rendering
- [ ] Particle effects
- [ ] HUD displays
- [ ] Anomaly visualization

### Phase 7: Commands & Tools
- [ ] Debug commands
- [ ] Player commands
- [ ] Telemetry system

### Phase 8: Polishing & Testing
- [ ] Compilation and build
- [ ] Runtime testing
- [ ] Performance optimization
- [ ] Documentation

## 9. API Surface

### 9.1 Public APIs

```java
// Get global RDP level
double globalRDP = RDPAPI.getGlobalRDPLevel();

// Get regional RDP
double regionalRDP = RDPAPI.getRegionalRDPLevel(chunkX, chunkZ);

// Get region pressure
double pressure = RDPAPI.getRegionPressure(chunkX, chunkZ);

// Add pressure to a region
RDPAPI.addRegionPressure(chunkX, chunkZ, 0.1);

// Get current RDP stage
GlobalRDPLevel.RDPStage stage = RDPAPI.getCurrentStage();

// Check if stage reached
boolean isRDPII = RDPAPI.hasReachedStage(GlobalRDPLevel.RDPStage.RDPII);

// Spawn anomaly
RDPAPI.spawnAnomaly(world, x, y, z, "SPATIAL", 0.5);

// Place reality anchor
RDPAPI.placeRealityAnchor(world, x, y, z);
```

### 9.2 Event Bus

```java
// Subscribe to RDP stage events
@Mod.EventHandler
public void onRDPStageChange(RDPStageChangeEvent event) {
    // event.oldStage, event.newStage, event.timestamp
}

// Subscribe to anomaly spawn
@Mod.EventHandler
public void onAnomalySpawn(AnomalySpawnEvent event) {
    // event.anomalyType, event.position, event.intensity
}

// Subscribe to judgement day
@Mod.EventHandler
public void onJudgementDay(JudgementDayEvent event) {
    // event.timestamp
}
```

## 10. Known Integration Challenges

### 10.1 Chunk Rewriter Queue Management
- **Challenge**: Multiple systems might request mutations simultaneously
- **Solution**: Central MutationCoordinator with priority queue

### 10.2 SRP Integration Complexity
- **Challenge**: SRP mods use various APIs for infection spreading
- **Solution**: Create adapter layer, handle version differences

### 10.3 Temporal Anomalies
- **Challenge**: Requires hooking into time-based systems
- **Solution**: Use TickEvent hooks, careful with performance

### 10.4 Dimensional Anomalies
- **Challenge**: Requires dimension-level state tracking
- **Solution**: Use DimensionManager events, per-dimension tracking

### 10.5 Client-Server Synchronization
- **Challenge**: RDP state needs to be client-aware for rendering
- **Solution**: Packet-based updates, selective sync

## 11. Testing Strategy

- Unit tests for RDPRegion, GlobalRDPLevel
- Integration tests with ChunkRewriter API
- Full modpack runtime testing
- Performance profiling under heavy load
- Vanilla Minecraft compatibility validation

## 12. Future Extensions

- Datapack-based anomaly definitions
- Mod configuration via in-game menu
- Multiplayer synchronization
- Custom mutation profiles
- Anomaly effect plugins

---

**Document Version**: 1.0  
**Last Updated**: 2026-08-27  
**Status**: Active Development
