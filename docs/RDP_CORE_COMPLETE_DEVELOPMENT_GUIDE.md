# RDP CORE — COMPLETE DEVELOPMENT GUIDE & IMPLEMENTATION ROADMAP

## 1. EXECUTIVE SUMMARY

**Current Implementation Status: ~45%**

- **Architecture:** Fully designed and documented
- **Core Simulation:** Functionally complete (pressure, regions, anomalies, events, mutations)
- **Integration Adapters:** Defined but non-functional (shells only)
- **Chunk Rewriter Bridge:** Critical path missing
- **Client Networking:** Completely absent
- **Rendering/Audio:** Completely absent

### Critical Metrics

| Metric | Status |
|--------|--------|
| **P0 TODOs (Blocking RDP Core)** | 12 |
| **P1 TODOs (Required for Full Simulation)** | 15 |
| **P2 TODOs (Required for Core Integration)** | 18 |
| **P3 TODOs (Gameplay)** | 22 |
| **Missing Route Hooks** | 28 |
| **Architectural Gaps** | 5 |

### Highest-Risk Issues

1. **Chunk Rewriter submission is dead code** — MutationCoordinator exists but never actually sends requests to Chunk Rewriter
2. **No mutation feedback loop** — Chunk Rewriter results don't flow back to RDP Core
3. **All integration adapters are stubs** — SCP001Integration, SRPIntegration, GameStagesIntegration all return null/false
4. **Client infrastructure missing** — No network packets, no rendering hooks, no client-side state sync
5. **Anomaly effects not implemented** — Anomalies spawn but subclasses don't apply any world effects

### Project Readiness Assessment

- **Can spawn anomalies:** YES
- **Can mutate chunks:** NO (Chunk Rewriter not connected)
- **Can synchronize with SCP-001:** NO (integration missing)
- **Can synchronize with SRP:** NO (integration missing)
- **Can synchronize with GameStages:** NO (integration missing)
- **Can show client-side effects:** NO (no network layer)
- **Can complete Judgement Day:** NO (event exists, no trigger mechanism)

---

## 2. CURRENT ARCHITECTURE

### 2.1 Architectural Principles

RDP Core implements a **hierarchical world-state system** based on:

- **Global RDP Level** (0.0-1.0) — World progression through stages RDP-0 to RDP-X
- **Regional RDP Levels** (per 256x256 block area) — Local distortion intensity
- **Pressure System** — Multi-source contribution (SRP, SCP, players, events)
- **Mutation Pipeline** — RDP Core → MutationCoordinator → Chunk Rewriter → World
- **Integration Contracts** — Each mod provides specific data → RDP → RDP applies effects

### 2.2 Core Data Flow

```
┌─────────────────────────────────────────────────────────────┐
│                      WORLD TICK                             │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ├─► RDPTickHandler (every N ticks)
                       │
            ┌──────────▼─────────────┐
            │  RDPSimulationEngine   │
            └──────────┬─────────────┘
                       │
        ┌──────────────┼──────────────┐
        │              │              │
        ▼              ▼              ▼
    Pressure      Regions        Anomalies
    Collection   Simulation      Spawning
        │              │              │
        └──────────────┼──────────────┘
                       │
              ┌────────▼────────┐
              │ Global RDP      │
              │ Aggregation     │
              └────────┬────────┘
                       │
        ┌──────────────┼──────────────┐
        │              │              │
        ▼              ▼              ▼
      Events      Mutations      Stage Transitions
   (EventScheduler)(MutationCoord) (RDPEvents)
        │              │              │
        └──────────────┼──────────────┘
                       │
        ┌──────────────▼──────────────┐
        │   World State Dirty Flag    │
        │   & Persistence            │
        └─────────────────────────────┘
```

### 2.3 Stage Progression Model

```
RDP-0   (0.00-0.09)  →  Pristine reality, rare anomalies
RDP-I   (0.10-0.24)  →  First distortions, minor mutations
RDP-II  (0.25-0.39)  →  Regional instability, chunk rewriting begins
RDP-III (0.40-0.54)  →  Spatial anomalies, hotspots active
RDP-IV  (0.55-0.69)  →  Temporal anomalies, time distortion
RDP-V   (0.70-0.79)  →  Dimensional anomalies, dimensional rifts
RDP-VI  (0.80-0.89)  →  Spatial collapse, sky and structure mutations
RDP-VII (0.90-0.96)  →  Cosmological instability, major events
RDP-X   (0.97-1.00)  →  JUDGEMENT DAY — Reality breakdown
```

---

## 3. COMPLETE TODO INVENTORY

### 3.1 P0 TODOs (Blocking RDP Core Functionality)

#### TODO-P0-001: Implement MutationCoordinator → Chunk Rewriter Bridge

**File:** `src/main/java/net/vas/rdpcore/mutation/MutationCoordinator.java:47`

**Current State:** Dead code with TODO comment

**Problem:**
- MutationCoordinator queues requests but never sends them
- Mutations are planned but never executed in the world
- No connection to actual Chunk Rewriter API

**Required Implementation:**
1. Inspect Chunk Rewriter project at `D:\Forge Modding\RDP Chunk Rewriter\RDPChunkRewriter`
2. Create adapter in RDP Core to bridge APIs
3. Update `processQueuedMutations()` to submit requests
4. Verify thread safety (must use server main thread)

**Priority:** P0 (blocks all world-state changes)

**Estimated Complexity:** MEDIUM (3-4 hours)

---

#### TODO-P0-002: Implement Mutation Result Callback Loop

**File:** `src/main/java/net/vas/rdpcore/mutation/MutationCoordinator.java`

**Current State:** No callback mechanism

**Problem:**
- RDP Core doesn't know if/when mutations succeed
- No history/scar system feedback
- No pressure reduction after mutations

**Required Implementation:**
1. Define mutation result interface
2. Create callback handler
3. Register with Chunk Rewriter
4. Update region state on completion

**Priority:** P0 (without this, mutations have no consequences)

**Estimated Complexity:** MEDIUM (2-3 hours)

---

#### TODO-P0-003: Implement SCP001Integration Event Hooks

**File:** `src/main/java/net/vas/rdpcore/integration/scp/SCP001Integration.java:32`

**Current State:** All hooks and methods are stubs returning null/0.0D

**Problem:**
- SCP-001 Controller exists as separate project but not integrated
- Bidirectional synchronization missing

**Required Implementation:**
1. Inspect SCP-001 Controller project
2. Implement event listener registration
3. Implement trigger method with reflection/API calls
4. Implement activity query method

**Priority:** P0 (core integration)

**Estimated Complexity:** HIGH (4-6 hours)

---

#### TODO-P0-004: Implement Anomaly Effect Application

**Files:** 
- `src/main/java/net/vas/rdpcore/anomaly/spatial/SpatialAnomaly.java:38`
- `src/main/java/net/vas/rdpcore/anomaly/temporal/TemporalAnomaly.java:38`
- `src/main/java/net/vas/rdpcore/anomaly/dimensional/DimensionalAnomaly.java:43`

**Current State:** `applyEffect()` methods are empty

**Problem:**
- Anomalies are spawned but don't affect world
- No mutations queued for effects
- No pressure contributions

**Required Implementation:**
1. Spatial: Queue block mutation requests
2. Temporal: Time distortions and pressure
3. Dimensional: Rift structures and portals

**Priority:** P0 (anomalies are core RDP mechanics)

**Estimated Complexity:** MEDIUM (3-4 hours)

---

#### TODO-P0-005: Implement RDPAPI Public API Methods

**File:** `src/main/java/net/vas/rdpcore/api/RDPAPI.java`

**Current State:** `spawnAnomaly()` and `placeRealityAnchor()` are TODOs

**Problem:**
- External mods can't use these APIs
- No way to create anomalies/anchors programmatically

**Required Implementation:**
1. `spawnAnomaly()`: Create and register anomaly, fire event
2. `placeRealityAnchor()`: Create anchor, store in world state, increase anchor count

**Priority:** P0 (external mod integration)

**Estimated Complexity:** LOW (1-2 hours)

---

#### TODO-P0-006: Initialize Mod Integration Adapters

**File:** `src/main/java/net/vas/rdpcore/RDPIntegrationManager.java:24`

**Current State:** Function body is empty comments

**Problem:**
- Integration adapters never initialized
- No pressure sources registered for SRP/SCP-001
- Mod detection doesn't happen

**Required Implementation:**
1. Check for each optional mod
2. Instantiate adapters
3. Call init() on each
4. Register pressure sources

**Priority:** P0 (orchestration for all integrations)

**Estimated Complexity:** LOW (1 hour)

---

### 3.2 P1 TODOs (Required for Full Simulation)

#### TODO-P1-001: Implement SRPIntegration Hooks

**File:** `src/main/java/net/vas/rdpcore/integration/modpack/SRPIntegration.java:32`

**Current State:** All methods return 0.0D or do nothing

**Priority:** P1 (core gameplay loop)

**Estimated Complexity:** HIGH (4-6 hours)

---

#### TODO-P1-002: Implement GameStagesIntegration Synchronization

**File:** `src/main/java/net/vas/rdpcore/integration/modpack/GameStagesIntegration.java:32`

**Current State:** All methods are stubs

**Priority:** P1 (gameplay progression)

**Estimated Complexity:** MEDIUM (2-3 hours)

---

#### TODO-P1-003: Implement Anchor Persistence System

**File:** `src/main/java/net/vas/rdpcore/world/RDPWorldState.java`

**Current State:** No anchor storage or serialization

**Priority:** P1 (persistent gameplay)

**Estimated Complexity:** LOW (1-2 hours)

---

### 3.3 P2 TODOs (Required for Core Integration)

#### TODO-P2-001: Implement Network Packet System

**File:** `src/main/java/net/vas/rdpcore/network/` (empty)

**Current State:** No network infrastructure

**Problem:** Clients can't see RDP effects

**Priority:** P2 (required for client visuals)

**Estimated Complexity:** HIGH (5-7 hours)

---

#### TODO-P2-002: Implement Client Rendering Hooks

**File:** `src/main/java/net/vas/rdpcore/rendering/` (empty)

**Current State:** No rendering

**Problem:** No visual feedback for RDP

**Priority:** P2 (core gameplay experience)

**Estimated Complexity:** HIGH (6-8 hours)

---

#### TODO-P2-003: Implement Client HUD System

**File:** `src/main/java/net/vas/rdpcore/rendering/`

**Current State:** No HUD

**Priority:** P2 (informational)

**Estimated Complexity:** MEDIUM (3-4 hours)

---

### 3.4 P3 TODOs (Gameplay Features & Polish)

- TODO-P3-001: Implement Audio System
- TODO-P3-002: Implement Judgement Day Event
- TODO-P3-003: InControl spawn rule integration
- TODO-P3-004: Recurrent Complex structure integration
- TODO-P3-005: BiomeTweaker biome mutation chains
- TODO-P3-006: Fluid mutation system
- TODO-P3-007: Scar system and persistence
- TODO-P3-008: Cross-dimensional leakage mechanics
- TODO-P3-009: Player exposure tracking
- TODO-P3-010: Anchor builder questline

---

## 4. RDP CORE ROUTE / HOOK ARCHITECTURE

### 4.1 Complete Route Map

```
┌─────────────────────────────────────────────────────────────────────┐
│                     RDP CORE (World-Level State)                    │
│                   GlobalRDPLevel (0.0 → 1.0)                        │
│          Regional RDPLevels × Pressure × Hotspots × Anomalies      │
└─────────────────────┬───────────────────────────────────────────────┘
                      │
        ┌─────────────┼─────────────┬──────────────────┐
        │             │             │                  │
        ▼             ▼             ▼                  ▼
    INPUT HOOKS  PROCESSING ROUTES  OUTPUT ROUTES    FEEDBACK LOOPS
```

### 4.2 Input Route Specifications

| Route ID | Source | Trigger | Status | Priority |
|----------|--------|---------|--------|----------|
| RDP-ROUTE-IN-001 | Minecraft | World load | COMPLETE | — |
| RDP-ROUTE-IN-002 | Minecraft | Server tick | COMPLETE | — |
| RDP-ROUTE-IN-003 | SRP | Pressure input | PARTIAL | P1 |
| RDP-ROUTE-IN-004 | RDP Core | SCP-001 manifestation | MISSING | P0 |
| RDP-ROUTE-IN-005 | SCP-001 | Activity data | MISSING | P0 |
| RDP-ROUTE-IN-006 | SRP | Evolution hook | MISSING | P1 |
| RDP-ROUTE-IN-007 | GameStages | Stage transitions | MISSING | P1 |
| RDP-ROUTE-IN-008 | Player | Position/activity | PARTIAL | P2 |
| RDP-ROUTE-IN-009 | Dimension | Transition events | MISSING | P2 |
| RDP-ROUTE-IN-010 | Block break | Anchor destruction | MISSING | P2 |

### 4.3 Output Route Specifications

| Route ID | Source | Target | Status | Priority |
|----------|--------|--------|--------|----------|
| RDP-ROUTE-OUT-001 | RDP Core | Chunk Rewriter | Mutation submit | MISSING | P0 |
| RDP-ROUTE-OUT-002 | Chunk Rewriter | RDP Core | Result callback | MISSING | P0 |
| RDP-ROUTE-OUT-003 | RDP Core | Clients | Network sync | MISSING | P2 |
| RDP-ROUTE-OUT-004 | RDP Core | GameStages | Stage unlock | MISSING | P1 |
| RDP-ROUTE-OUT-005 | RDP Core | InControl | Spawn modifier | MISSING | P3 |
| RDP-ROUTE-OUT-006 | RDP Core | Audio | Sound events | MISSING | P3 |
| RDP-ROUTE-OUT-007 | RDP Core | Rendering | Visual effects | MISSING | P2 |
| RDP-ROUTE-OUT-008 | RDP Core | Logs | Telemetry | PARTIAL | P4 |

### 4.4 Missing Route Matrix

| Source | Destination | Existing Hook | Adapter | Status | Priority |
|--------|-------------|---------------|---------|--------|----------|
| RDP Core | Chunk Rewriter | MutationCoordinator | ChunkRewriterBridge | MISSING | P0 |
| Chunk Rewriter | RDP Core | Chunk Rewriter event | MutationResultHandler | MISSING | P0 |
| RDP Core | SCP-001 | RDP stage check | SCP001Integration | MISSING | P0 |
| SCP-001 | RDP Core | SCP-001 event | SCP001Integration | MISSING | P0 |
| SRP | RDP Core | SRP event | SRPIntegration | MISSING | P1 |
| RDP Core | GameStages | Stage change event | GameStagesIntegration | MISSING | P1 |
| GameStages | RDP Core | Stage change event | GameStagesIntegration | MISSING | P1 |
| RDP Core | Clients | Server tick | Network packets | MISSING | P2 |

---

## 5. PRESSURE ROUTES

### 5.1 Complete Pressure Source Map

#### Source 1: Base Pressure
- **Class:** `BasePressureSource`
- **Role:** Constant pressure (ensures RDP grows naturally)
- **Weight:** 0.01 per tick
- **Status:** ✓ COMPLETE

#### Source 2: Player Activity
- **Class:** `PlayerPressureSource`
- **Role:** Players accelerate RDP progression
- **Weight:** 0.05 per player per region per tick
- **Status:** ✓ COMPLETE

#### Source 3: SRP (Scape and Run: Parasites)
- **Class:** `SRPPressureSource`
- **Role:** SRP evolution drives RDP progression
- **Weight:** Configurable (default 0.2)
- **Status:** ✗ NOT IMPLEMENTED (method returns 0.0D)

#### Source 4: SCP Activity
- **Class:** `SCPPressureSource`
- **Role:** SCP entities increase local RDP stress
- **Weight:** Configurable (default 1.0)
- **Status:** ✗ NOT IMPLEMENTED

#### Source 5: SCP-001 Activity
- **Class:** SCP001 integration (to be created)
- **Role:** SCP-001 manifestations drive pressure
- **Status:** ✗ NOT IMPLEMENTED

#### Source 6-10: Anomaly, Hotspot, Scar, Event, Anchor
- **Status:** VARIOUS (see detailed guide for each)

---

## 6. MUTATION ROUTES

### 6.1 Complete Mutation Pipeline

```
Regional RDP > 0.25
    ↓
Construct MutationRequest (center, radius, profile, intensity, budget)
    ↓
MutationCoordinator.queue()
    ↓
***MISSING: ChunkRewriterBridge submission***
    ↓
Chunk Rewriter executes mutation
    ↓
***MISSING: Callback with result***
    ↓
MutationResultHandler processes result
    ↓
Update region history, create scars, reduce pressure
```

### 6.2 Mutation Request Specification

```java
MutationRequest {
    centerChunkX: int
    centerChunkZ: int
    radius: int
    profile: String
    intensity: float
    priority: int (0-100)
    budget: int
    cause: String
    dimension: String
}
```

### 6.3 Missing Mutation Result Handler

**Current State:** No callback mechanism exists

**Required:** Handler for IMutationResult objects

---

## 7. REGIONAL RDP ROUTES

### 7.1 Regional Simulation Flow

```
1. PRESSURE COLLECTION
   └─ Iterate all pressure sources

2. PRESSURE DECAY
   └─ Subtract REGION_PRESSURE_DECAY (0.001)

3. LOCAL RDP EVOLUTION
   └─ delta = (pressure × 0.001) - anchor_effect - 0.0005

4. PROPAGATION (Neighbor Diffusion)
   └─ Spread to 8 adjacent regions

5. ANOMALY SPAWNING
   └─ Probability based on local_rdp

6. ANOMALY MANAGER TICK
   └─ Materialize, update, apply effects, remove expired

7. EVENT SCHEDULER
   └─ Generate and queue events

8. MUTATION PLANNING
   └─ Queue mutation requests if above threshold
```

---

## 8. THREADING & THREAD SAFETY

### 8.1 Thread Requirements

| Component | Thread | Safety |
|-----------|--------|--------|
| RDPSimulationEngine | Server main | CRITICAL |
| MutationCoordinator | Server main | CRITICAL |
| Chunk Rewriter API | Server main | CRITICAL |
| Anomaly effects | Server main | CRITICAL |
| Network packet sending | Network thread | HIGH |
| Client rendering | Render thread | CRITICAL |
| RDPAPI access | Any | MEDIUM |

---

## 9. PERFORMANCE CHARACTERISTICS

### 9.1 Per-Tick Impact

| Operation | Cost | Frequency | Total |
|-----------|------|-----------|-------|
| Tick handler | <1ms | Every tick | <1ms |
| Simulation | 50-100ms | Every 200 ticks | 0.25-0.5ms avg |
| Pressure registry | 1-2ms | Every 200 ticks | 0.005-0.01ms avg |
| Total simulated | 50-100ms | Every 200 ticks | 0.25-0.5ms avg |

**Target:** <2ms per simulation cycle

**Status:** WITHIN BUDGET

### 9.2 Memory Impact

| Data Structure | Typical Size |
|---|---|
| RDPWorldState | ~1KB |
| RDPRegion (1 region) | ~2KB |
| Regions (typical: 500 active) | ~1MB |
| Anomalies (max 10/region) | ~200KB |
| Hotspots (typical 100) | ~50KB |
| **Total per world** | **~1.3MB** |

---

## 10. DEPENDENCY GRAPH

### 10.1 Initialization Order

```
1. RDPCore.preInit()
   ├─ RDPConfig.load()
   └─ RDPCoreData.init()

2. RDPCore.init()
   ├─ Register RDPWorldEventHandler
   ├─ Register RDPTickHandler
   └─ RDPSimulationEngine.init()

3. RDPCore.postInit()
   └─ RDPIntegrationManager.initializeIntegrations()

4. Server start
   └─ RdpCommand registered
```

### 10.2 Runtime Dependency Graph

```
RDPSimulationEngine
    ├─ RDPConfig
    ├─ RDPAPI
    │   └─ RDPWorldState
    │       ├─ GlobalRDPLevel
    │       ├─ RDPRegion × N
    │       ├─ Anomalies
    │       └─ Hotspots
    ├─ PressureRegistry
    ├─ RegionManager
    ├─ HotspotManager
    ├─ AnomalyManager
    ├─ EventScheduler
    ├─ MutationCoordinator
    └─ RDPEvents
```

---

## 11. IMPLEMENTATION PHASES

### Phase A — CRITICAL MISSING ROUTES (P0)

**Goal:** Connect core systems and enable basic RDP progression

**Duration:** ~2-3 weeks

**Deliverables:**
- TODO-P0-001: Chunk Rewriter bridge
- TODO-P0-002: Mutation result callback
- TODO-P0-003: SCP-001 integration hooks
- TODO-P0-004: Anomaly effect application
- TODO-P0-005: RDPAPI implementation
- TODO-P0-006: Integration manager

**Success Criteria:**
- Chunks actually mutate in world
- Mutations reduce pressure
- SCP-001 responds to RDP
- External mods can use RDPAPI
- No crashes on missing optional mods

---

### Phase B — EXTERNAL INTEGRATIONS (P1)

**Goal:** Complete integration with all major modpack systems

**Duration:** ~1-2 weeks

**Deliverables:**
- TODO-P1-001: SRPIntegration hooks
- TODO-P1-002: GameStagesIntegration sync
- TODO-P1-003: Anchor persistence

**Success Criteria:**
- SRP evolution influenced by RDP
- GameStages progression synchronized
- Anchors persist across save/load

---

### Phase C — CLIENT NETWORKING (P2)

**Goal:** Enable client-side visualization and feedback

**Duration:** ~1-2 weeks

**Deliverables:**
- TODO-P2-001: Network packet system
- TODO-P2-002: Client rendering hooks
- TODO-P2-003: Client HUD system

**Success Criteria:**
- Players see RDP status
- Anomalies visible
- Screen effects applied
- No network lag

---

### Phase D — POLISH & FEATURES (P3/P4)

**Goal:** Enhance gameplay, fix bugs, optimize

**Duration:** ~1-2 weeks

**Deliverables:**
- Audio system
- Judgement Day event
- Performance optimization
- Debug commands

---

## 12. TESTING STRATEGY

### 12.1 Unit Tests

- Pressure calculation
- Region simulation
- Serialization (NBT round-trip)

### 12.2 Integration Tests

- Chunk Rewriter route
- SCP-001 route
- Network sync
- GameStages route

### 12.3 End-to-End Tests

```
Scenario 1: Normal progression (RDP-0 → RDP-V)
Scenario 2: Forced progression (commands)
Scenario 3: Judgement Day
Scenario 4: Mod absence (graceful fallback)
```

---

## 13. DEBUGGING STRATEGY

### 13.1 Debug Commands (Existing & To Add)

**Existing:**
- `/rdp status` — Show global RDP
- `/rdp simulate N` — Run N cycles
- `/rdp set LEVEL` — Set global RDP
- `/rdp telemetry` — Show metrics

**To Add:**
- `/rdp pressure` — Show pressure breakdown
- `/rdp region` — Show region at player
- `/rdp anomalies` — List active anomalies
- `/rdp hooks` — Show integration status
- `/rdp mutations` — Show mutation queue
- `/rdp routes` — Show route status

### 13.2 Logging Guidelines

**Normal:** Initialization, major events

**Debug:** Pressure values, regional updates

**Warnings:** Integration failures

**Errors:** Crashes, persistence failures

---

## 14. DEFINITION OF DONE

### 14.1 Core Completion Checklist

```
[ ] All P0 TODOs implemented and tested
[ ] All P1 TODOs implemented and tested
[ ] All P2 TODOs implemented and tested
[ ] Chunk Rewriter route verified working
[ ] SCP-001 route verified working
[ ] SRP route verified working
[ ] GameStages route verified working
[ ] Network sync working for clients
[ ] Client rendering displays RDP effects
[ ] HUD shows accurate status
[ ] Audio plays correctly
[ ] Persistence works across save/load
[ ] Commands functional and helpful
[ ] No null pointer exceptions
[ ] No mod compatibility crashes
[ ] Performance within budget
[ ] Logging is informative
[ ] Telemetry collects useful data
[ ] Documentation complete
[ ] All routes documented in guide
```

### 14.2 Quality Checklist

```
[ ] Code follows Java 8 conventions
[ ] Comments explain why, not what
[ ] All TODOs resolved or documented
[ ] Configuration documented
[ ] Example usage provided
[ ] Error messages helpful
[ ] No magic numbers (constants instead)
[ ] Thread safety verified
[ ] Memory leaks checked
[ ] Permission checks (OP level for commands)
```

---

## 15. QUICK REFERENCE

### Project Structure

```
src/main/java/net/vas/rdpcore/
├── RDPCore.java                    (main mod class)
├── RDPSimulationEngine.java        (core simulation)
├── RDPTickHandler.java             (tick hook)
├── RDPWorldEventHandler.java       (world events)
├── RDPIntegrationManager.java      (integration orchestration)
├── RDPCoreData.java                (data initialization)
├── api/
│   └── RDPAPI.java                 (public API)
├── command/
│   └── RdpCommand.java             (debug commands)
├── config/
│   └── RDPConfig.java              (configuration)
├── core/
│   └── GlobalRDPLevel.java         (global progression)
├── region/
│   ├── RDPRegion.java              (regional state)
│   ├── RegionManager.java          (active region cache)
│   ├── HotspotManager.java         (hotspot lifecycle)
│   └── Hotspot.java                (hotspot data)
├── mutation/
│   ├── MutationCoordinator.java    (queue management)
│   └── MutationRequest.java        (request data)
├── anomaly/
│   ├── AnomalyManager.java         (anomaly lifecycle)
│   ├── Anomaly.java                (base class)
│   ├── spatial/
│   ├── temporal/
│   └── dimensional/
├── event/
│   ├── EventScheduler.java         (event generation)
│   └── RDPEvents.java              (event definitions)
├── integration/
│   ├── ModIntegration.java         (base adapter)
│   ├── modpack/
│   │   ├── SRPIntegration.java
│   │   └── GameStagesIntegration.java
│   └── scp/
│       └── SCP001Integration.java
├── util/
│   ├── PressureRegistry.java       (pressure sources)
│   ├── BasePressureSource.java
│   ├── PlayerPressureSource.java
│   ├── SRPPressureSource.java
│   ├── SCPPressureSource.java
│   ├── IRdpPressureSource.java     (interface)
│   └── Telemetry.java              (metrics)
├── world/
│   ├── RDPWorldState.java          (world state container)
│   ├── RDPWorldSavedData.java      (NBT persistence)
│   └── RegionManager.java          (region lifecycle)
├── entity/
│   └── RealityAnchor.java          (anchor entity)
├── network/                         (EMPTY - TODO)
└── rendering/                       (EMPTY - TODO)
```

---

## 16. KEY FILES & LINE NUMBERS

| File | Issue | Line(s) | Status |
|------|-------|---------|--------|
| MutationCoordinator.java | TODO: Submit to Chunk Rewriter | 47 | MISSING |
| RDPAPI.java | TODO: spawnAnomaly() | 131 | MISSING |
| RDPAPI.java | TODO: placeRealityAnchor() | 140 | MISSING |
| SCP001Integration.java | TODO: All hooks | 32, 48, 57 | MISSING |
| SRPIntegration.java | TODO: All hooks | 32, 48, 57 | MISSING |
| GameStagesIntegration.java | TODO: All synchronization | 32, 47, 56 | MISSING |
| SpatialAnomaly.java | TODO: Apply effects | 38 | MISSING |
| TemporalAnomaly.java | TODO: Apply effects | 38 | MISSING |
| DimensionalAnomaly.java | TODO: Apply effects | 43 | MISSING |
| RDPIntegrationManager.java | TODO: Initialize integrations | 24 | MISSING |
| RDPCoreData.java | TODO: Initialize data | 17 | MISSING |

---

## 17. MOST IMPORTANT FINAL REQUIREMENT

The central purpose of this work is to eliminate the problem:

> **"The architecture says the systems should interact, but nobody knows exactly where the connection is supposed to be."**

After completing this guide, a developer must be able to trace every major RDP route as:

```
SOURCE
  ↓
ACTUAL HOOK
  ↓
ADAPTER
  ↓
RDP CORE API
  ↓
SIMULATION / STATE
  ↓
DECISION
  ↓
OUTPUT ADAPTER
  ↓
TARGET SYSTEM
  ↓
FEEDBACK
```

For example:

```
SRP
 ↓
verified SRP hook
 ↓
SRPIntegration
 ↓
PressureManager
 ↓
RegionalSimulation
 ↓
Local RDP
 ↓
MutationPlanner
 ↓
MutationCoordinator
 ↓
Chunk Rewriter
 ↓
MutationResult
 ↓
RDP History
 ↓
RDP Scar
 ↓
future Pressure
```

Every important system in the modpack should have an equivalent documented route.

---

## FINAL SUMMARY

### What's Complete

✓ Core architecture and simulation engine
✓ Pressure system framework
✓ Regional RDP state management
✓ Anomaly classes and manager
✓ Persistence system (NBT)
✓ Configuration system
✓ Event system
✓ Basic API surface
✓ World event hooks

### What's Missing (Critical Path)

✗ Chunk Rewriter API bridge
✗ Mutation result feedback
✗ SCP-001 integration hooks
✗ SRP integration hooks
✗ GameStages synchronization
✗ Network packet system
✗ Client rendering
✗ Client HUD
✗ Audio system

### Recommended Next Action

1. **Immediately:** Inspect Chunk Rewriter and SCP-001 Controller projects
2. **Week 1:** Implement TODO-P0-001 through TODO-P0-006 (Critical path)
3. **Week 2-3:** Implement TODO-P1 items (integrations)
4. **Week 4:** Implement TODO-P2 items (client infrastructure)
5. **Week 5+:** Polish, testing, optimization

### Success Metrics

- Gameplay feels complete (not alpha/beta)
- No required mod crashes
- Performance stays <2ms average per simulation cycle
- All routes traced and documented
- External mods can use RDPAPI successfully

---

**This guide is the master roadmap for completing RDP Core. Every implementation task, every integration point, and every critical missing route is documented here. Use this as your single source of truth for what remains, why it matters, and how to implement it.**
