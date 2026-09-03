# RDP Core Public API Reference

**Version**: 1.0.0  
**Package**: `net.vas.rdpcore.api`  
**Stability**: Stable for Phase 1-7

---

## Overview

RDP Core exposes a public API through `RDPAPI` class for external mod integrations. This allows other mods to:
- Query current RDP state
- Contribute pressure to the system
- Implement custom anomalies or effects
- React to RDP progression

---

## Global State Access

### Getting Global RDP Level
```java
double level = RDPAPI.getGlobalRDPLevel(World world);
// Returns: 0.0 to 1.0+ (may exceed 1.0 for extreme cases)
```

### Setting Global RDP Level
```java
RDPAPI.setGlobalRDPLevel(World world, double level);
```

**Caution**: Direct modification bypasses simulation. Use `addGlobalRDP()` for normal gameplay.

### Incrementing Global RDP
```java
RDPAPI.addGlobalRDP(World world, double delta);
// Safe: Works with stage multipliers and simulation
```

### Getting Current Stage
```java
GlobalRDPLevel.RDPStage stage = RDPAPI.getCurrentStage(World world);
// Returns: RDP0, RDPI, RDPII, ... RDPX
```

### Checking Stage Threshold
```java
boolean reached = RDPAPI.hasReachedStage(World world, GlobalRDPLevel.RDPStage.RDPIV);
// Returns: true if global RDP ≥ threshold for that stage
```

### Checking Judgement Day
```java
boolean active = RDPAPI.isJudgementDayActive(World world);
// Returns: true if Judgement Day (RDP-X end-state) is active
```

---

## Regional State Access

### Get or Create Region
```java
RDPRegion region = RDPAPI.getOrCreateRegion(World world, int chunkX, int chunkZ);
// Automatically creates region if it doesn't exist
```

### Get Regional RDP Level
```java
double level = RDPAPI.getRegionalRDPLevel(World world, int chunkX, int chunkZ);
// Returns: 0.0 to 1.0+ (region-local value)
```

### Set Regional RDP Level
```java
RDPAPI.setRegionalRDPLevel(World world, int chunkX, int chunkZ, double level);
```

### Add Regional RDP
```java
RDPAPI.addRegionalRDP(World world, int chunkX, int chunkZ, double delta);
// Safe: Works with decay and propagation
```

### Get Regional Pressure
```java
double pressure = RDPAPI.getRegionPressure(World world, int chunkX, int chunkZ);
// Returns: 0.0 to 1.0+ (combined pressure from all sources)
```

### Add Regional Pressure
```java
RDPAPI.addRegionPressure(World world, int chunkX, int chunkZ, double delta);
// Contributes to pressure calculation
```

---

## Anomalies (Phase 2+)

### Spawn Anomaly
```java
RDPAPI.spawnAnomaly(World world, int x, int y, int z, String type, double intensity);
// Type: "SPATIAL", "TEMPORAL", or "DIMENSIONAL"
// Intensity: 0.0 to 1.0+
// Status: Deferred - not yet implemented
```

**Note**: Currently a placeholder. Will be implemented in Phase 8+.

---

## Reality Anchors (Phase 2+)

### Place Reality Anchor
```java
RealityAnchor anchor = RDPAPI.placeRealityAnchor(World world, int x, int y, int z);
// Returns: Anchor object for tracking
// Status: Deferred - not yet implemented
```

**Note**: Currently a placeholder. Will be implemented in Phase 8+.

---

## Pressure Source Registration

External mods can contribute pressure to RDP via `IRdpPressureSource` interface:

```java
public class MyCustomPressureSource implements IRdpPressureSource {
    
    private double weight;
    
    public MyCustomPressureSource(double weight) {
        this.weight = weight;
    }
    
    @Override
    public double getPressure(World world, Object context) {
        try {
            // Calculate pressure based on world state
            // context is the region being evaluated (can be null for global)
            
            double myPressure = calculateMyPressure(world);
            return Math.min(1.0D, myPressure * weight);
            
        } catch (Throwable t) {
            // Always return 0.0 if there's any issue
            // Never throw exceptions from pressure sources
            return 0.0D;
        }
    }
    
    private double calculateMyPressure(World world) {
        // Your custom pressure calculation
        // Return 0.0 to 1.0 (can exceed 1.0 for extreme values)
        return 0.0D;
    }
}
```

### Register Pressure Source
```java
PressureRegistry.register(new MyCustomPressureSource(0.5D));
// Weight: 0.5 means contributes 50% of calculated value
```

**Best Practices**:
- Return 0.0 if your mod is not present
- Catch all exceptions and return 0.0
- Use weight < 1.0 for most sources (so no single source dominates)
- Cache expensive calculations (20-tick cache is reasonable)
- Only recalculate when needed

### Example: Parasite Counting Pressure
```java
public class ParasitePressure implements IRdpPressureSource {
    
    @Override
    public double getPressure(World world, Object context) {
        try {
            if (!Loader.isModLoaded("srparasites")) {
                return 0.0D;
            }
            
            // Count parasites (simplified)
            int parasites = countParasitesInWorld(world);
            
            // Normalize: ~100 parasites = 1.0 pressure
            return Math.min(1.0D, parasites / 100.0D);
            
        } catch (Throwable t) {
            return 0.0D;
        }
    }
    
    private int countParasitesInWorld(World world) {
        int count = 0;
        for (Entity entity : world.loadedEntityList) {
            if (isParasite(entity)) {
                count++;
            }
        }
        return count;
    }
    
    private boolean isParasite(Entity entity) {
        // Check entity type
        String className = entity.getClass().getName();
        return className.contains("Parasite") || 
               className.contains("nex.entity");
    }
}
```

---

## Event Listening

### RDP Stage Changed Event
```java
@SubscribeEvent
public void onRDPStageChanged(RDPEvents.RDPStageChangedEvent event) {
    WorldServer world = event.world;
    GlobalRDPLevel.RDPStage newStage = event.newStage;
    
    // React to stage change
    // Example: Trigger mod-specific events
}
```

### World State Event
```java
@SubscribeEvent
public void onRDPWorldInitialized(RDPEvents.RDPWorldInitEvent event) {
    WorldServer world = event.world;
    RDPWorldState state = event.state;
    
    // World RDP state is ready
}
```

---

## World State Snapshot

### Get World State
```java
RDPWorldState state = RDPAPI.getWorldState(World world);

// Access components
GlobalRDPLevel global = state.getGlobalRDPLevel();
Map<Long, RDPRegion> regions = state.getAllRegions();
Map<String, Scar> scars = state.getScars();
WorldMutationHistory history = state.getMutationHistory();
```

### Save World State
```java
RDPAPI.saveWorldState(World world, RDPWorldState state);
// Marks state dirty, triggers write to NBT on next save
```

---

## Mutation System Integration

### Queue Mutation Request
```java
MutationRequest request = MutationRequest.builder()
    .center(chunkX, chunkZ)           // Region center
    .radius(16)                       // Region size
    .profile("rdp_regional_growth")   // Profile name
    .intensity(0.6F)                  // 0.0-1.0
    .priority(10)                     // Higher = sooner
    .budget(100)                      // Max blocks/chunks
    .cause("MY_MOD_EVENT")            // Event source
    .dimension(world.provider.getDimensionType().getName())
    .build();

MutationCoordinator.getInstance().queueMutation(request, priority);
```

### Check Mutation Status
```java
MutationCoordinator coordinator = MutationCoordinator.getInstance();

int queued = coordinator.getQueuedCount();
int accepted = coordinator.getTotalAccepted();
int rejected = coordinator.getTotalRejected();
long lastTime = coordinator.getLastExecutionTime();

// Report metrics
```

---

## Configuration Access

### Read Configuration
```java
import net.vas.rdpcore.config.RDPConfig;

double threshold = RDPConfig.CHUNK_REWRITE_THRESHOLD;
int interval = RDPConfig.SIMULATION_INTERVAL_TICKS;
int regionSize = RDPConfig.REGION_SIZE_CHUNKS;
```

### Common Configurations
- `SIMULATION_INTERVAL_TICKS`: How often simulation runs
- `CHUNK_REWRITE_THRESHOLD`: RDP level to start mutations
- `REGION_SIZE_CHUNKS`: Region granularity (16 = 256×256 blocks)
- `GLOBAL_RDP_INCREMENT_PER_TICK`: Base RDP growth rate
- `GLOBAL_RDP_STAGE_THRESHOLDS`: Per-stage progression thresholds

---

## Telemetry & Debugging

### Get Simulation Metrics
```java
net.vas.rdpcore.util.Telemetry telemetry = 
    net.vas.rdpcore.util.Telemetry.getInstance();

long lastSimulationMs = telemetry.getLastSimulationTime();
int anomaliesProcessed = telemetry.getLastAnomaliesProcessed();
int mutationsQueued = telemetry.getLastMutationsQueued();
```

### Logging
```java
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

Logger logger = LogManager.getLogger("rdpcore");

logger.info("My integration is working");
logger.warn("Something unexpected");
logger.error("Critical issue");
logger.debug("Debug information");
```

---

## Safety Guidelines

### Do's
- ✅ Call RDPAPI methods from any thread (safe)
- ✅ Return 0.0D if your mod is not present
- ✅ Cache expensive calculations (20-40 ticks)
- ✅ Catch all exceptions in pressure sources
- ✅ Use soft dependency patterns with `Loader.isModLoaded()`
- ✅ Log errors for debugging
- ✅ Use weight < 1.0 for custom pressure sources

### Don'ts
- ❌ Don't throw exceptions from `getPressure()`
- ❌ Don't scan all chunks every tick
- ❌ Don't scan all entities every tick (cache instead)
- ❌ Don't make blocking network calls from simulation
- ❌ Don't mutate world state from pressure calculation
- ❌ Don't assume other mods are present
- ❌ Don't exceed 1-2ms execution time per pressure source

---

## Example: Complete Mod Integration

```java
@Mod(modid = "myrpd", name = "My RDP Integration", version = "1.0")
public class MyRPDIntegration {
    
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        // Register pressure source during pre-init
        PressureRegistry.register(new MyPressureSource(0.3D));
    }
    
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        // Register event listeners
        MinecraftForge.EVENT_BUS.register(new MyRDPEventListener());
    }
}

public class MyPressureSource implements IRdpPressureSource {
    private double weight;
    
    public MyPressureSource(double weight) {
        this.weight = weight;
    }
    
    @Override
    public double getPressure(World world, Object context) {
        try {
            // Calculate custom pressure
            double custom = 0.5D;
            return Math.min(1.0D, custom * weight);
        } catch (Throwable t) {
            return 0.0D;
        }
    }
}

public class MyRDPEventListener {
    
    @SubscribeEvent
    public void onStageChanged(RDPEvents.RDPStageChangedEvent event) {
        // React to RDP progression
    }
}
```

---

## Compatibility Matrix

| API Method | Phase | Status | Notes |
|---|---|---|---|
| `getGlobalRDPLevel()` | 1 | ✅ Stable | Safe to use |
| `setGlobalRDPLevel()` | 1 | ✅ Stable | Bypasses simulation |
| `addGlobalRDP()` | 1 | ✅ Stable | Recommended |
| `getRegionalRDPLevel()` | 1 | ✅ Stable | Safe to use |
| `addRegionalRDP()` | 1 | ✅ Stable | Recommended |
| `spawnAnomaly()` | 2 | ⏳ Deferred | Placeholder |
| `placeRealityAnchor()` | 2 | ⏳ Deferred | Placeholder |
| Event listeners | 1 | ✅ Stable | Safe to use |
| Pressure source API | 1 | ✅ Stable | Safe to use |

---

## Migration Guide

### From Placeholder to Full Implementation
When Phase 2 arrives and anomaly/anchor APIs are implemented:
- Your code won't break
- Methods will start returning real objects
- Gracefully test with `if (anomaly != null)` checks

### Version Checking
```java
// Detect RDP Core presence
if (Loader.isModLoaded("rdpcore")) {
    // RDP Core is installed
    // Safe to use RDPAPI
}
```

---

## Support & Questions

For API questions or issues:
1. Check logs for `[RDP]` error messages
2. Verify RDPAPI methods are called from game thread
3. Test pressure source with try-catch wrapping
4. Use debug commands: `/rdp status`, `/rdp telemetry`, `/rdp debug`

---

**Last Updated**: August 2026  
**API Version**: 1.0  
**Stable**: Yes (for Phase 1-7 methods)
