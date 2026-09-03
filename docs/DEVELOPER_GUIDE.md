# R.D.P. Core Developer Guide

This guide explains how to develop features for RDP Core and integrate with the framework.

## Architecture Overview

### Layered Design

```
┌─────────────────────────────────────────┐
│        Public API (RDPAPI)              │ ← External mods use this
├─────────────────────────────────────────┤
│      Integration Adapters               │ ← Mod-specific adapters
│  (SCP001, SRP, GameStages, etc.)       │
├─────────────────────────────────────────┤
│     Core Simulation Engine              │ ← RDP progression logic
│   (GlobalRDP, Regions, Anomalies)      │
├─────────────────────────────────────────┤
│   World State Management                │ ← Persistence & storage
│   (NBT serialization, chunk data)      │
├─────────────────────────────────────────┤
│   Low-Level Forge Integration           │ ← Event handlers, tickers
│   (Events, Commands, Network)          │
└─────────────────────────────────────────┘
```

### Module Responsibilities

- **`core/`** - Global RDP level tracking (immutable logic)
- **`region/`** - Regional state management (isolation)
- **`world/`** - World-level orchestration (composition)
- **`anomaly/`** - Distortion systems (extensible)
- **`entity/`** - Reality anchor persistence (stable)
- **`mutation/`** - Chunk rewriter coordination (interface)
- **`integration/`** - External mod adapters (pluggable)
- **`simulation/`** - Engine tick processing (central)
- **`event/`** - Event definitions (dispatch)
- **`api/`** - Public surface (stable)

## Adding a New Feature

### Step 1: Define the Concept

Write a design document explaining:
- What the feature does
- How it fits into the RDP framework
- How it affects other systems
- Configuration options

Example: "Temporal Anomalies" feature

```markdown
## Temporal Anomalies

**Purpose**: Represent time-based reality distortions

**Effects**:
- Mobs age faster/slower
- Crops grow at abnormal rates
- Player damage/hunger behavior changes
- Blocks update at different speeds

**RDP Triggers**: 
- Spawn at RDP-IV (0.55+)
- Increase frequency as RDP increases
- Decay as intensity decreases

**Configuration**:
- enable_temporal_anomalies (default: true)
- temporal_anomaly_threshold (default: 0.55)
- temporal_max_radius_blocks (default: 64)
- temporal_effect_strength (default: 1.0)
```

### Step 2: Create Core Class

Define the core data structure:

```java
// src/main/java/net/vas/rdpcore/anomaly/temporal/TemporalAnomaly.java

public class TemporalAnomaly extends Anomaly {
    private double timeMultiplier = 1.0D;
    
    public TemporalAnomaly(int x, int y, int z, double intensity) {
        super(x, y, z, intensity);
        this.maxAge = 20 * 90;
    }
    
    @Override
    public String getAnomalyType() {
        return "TEMPORAL";
    }
    
    @Override
    public void applyEffect() {
        // Apply time distortion to entities/blocks
    }
    
    @Override
    public void tick() {
        super.tick();
        this.timeMultiplier = 1.0D + (intensity * 3.0D);
    }
}
```

### Step 3: Implement Generator

Create logic to spawn the anomaly:

```java
// src/main/java/net/vas/rdpcore/anomaly/temporal/TemporalAnomalyGenerator.java

public class TemporalAnomalyGenerator {
    
    /**
     * Determine if a temporal anomaly should spawn
     */
    public static boolean shouldSpawn(RDPRegion region, GlobalRDPLevel global) {
        if (!RDPConfig.ENABLE_TEMPORAL_ANOMALIES) return false;
        if (global.getLevel() < RDPConfig.TEMPORAL_ANOMALY_THRESHOLD) return false;
        
        // Probability increases with pressure
        double probability = region.getPressure() * 0.05D;
        return Math.random() < probability;
    }
    
    /**
     * Generate a temporal anomaly
     */
    public static TemporalAnomaly generate(World world, RDPRegion region) {
        // Pick random location in region
        int x = region.getRegionX() * 256 + (int)(Math.random() * 256);
        int z = region.getRegionZ() * 256 + (int)(Math.random() * 256);
        int y = world.getHeight(new BlockPos(x, 0, z)).getY();
        
        // Create with intensity based on pressure
        double intensity = Math.min(1.0D, region.getPressure());
        
        return new TemporalAnomaly(x, y, z, intensity);
    }
}
```

### Step 4: Integrate with Simulation

Add spawning logic to the simulation engine:

```java
// In RDPSimulationEngine.onTick()

for (RDPRegion region : worldState.getAllRegions().values()) {
    if (TemporalAnomalyGenerator.shouldSpawn(region, globalRDP)) {
        TemporalAnomaly anomaly = TemporalAnomalyGenerator.generate(world, region);
        region.addAnomaly(anomaly.getId(), anomaly);
        
        // Fire event
        MinecraftForge.EVENT_BUS.post(
            new AnomalySpawnEvent("TEMPORAL", anomaly.getX(), 
                anomaly.getY(), anomaly.getZ(), anomaly.getIntensity())
        );
    }
    
    // Tick existing anomalies
    for (Anomaly anomaly : region.getAnomalies().values()) {
        anomaly.tick();
        if (anomaly.isActive()) {
            anomaly.applyEffect();
        } else {
            region.removeAnomaly(anomaly.getId());
        }
    }
}
```

### Step 5: Add Configuration

Update `RDPConfig.java`:

```java
public static boolean ENABLE_TEMPORAL_ANOMALIES = true;
public static double TEMPORAL_ANOMALY_THRESHOLD = 0.55D;
public static int TEMPORAL_MAX_RADIUS_BLOCKS = 64;

// In load()
ENABLE_TEMPORAL_ANOMALIES = config.getBoolean(
    "Enable temporal anomalies",
    Configuration.CATEGORY_GENERAL,
    true,
    ""
);

TEMPORAL_ANOMALY_THRESHOLD = config.getFloat(
    "Temporal anomaly RDP threshold",
    Configuration.CATEGORY_GENERAL,
    0.55F,
    0.0F,
    1.0F,
    ""
);
```

### Step 6: Implement Effects

Implement the actual in-game effects:

```java
@SubscribeEvent
public static void onLivingUpdate(LivingUpdateEvent event) {
    EntityLivingBase entity = event.getEntityLiving();
    World world = entity.world;
    
    // Find if entity is in a temporal anomaly
    double rdp = RDPAPI.getRegionalRDPLevel(world, 
        entity.chunkCoordX, entity.chunkCoordZ);
    
    if (rdp >= RDPConfig.TEMPORAL_ANOMALY_THRESHOLD) {
        // Apply time distortion effect
        // Make entity age faster
        if (entity instanceof EntityLiving) {
            ((EntityLiving) entity).extinguishFire();
        }
    }
}
```

### Step 7: Write Tests

Create unit tests:

```java
// src/test/java/net/vas/rdpcore/anomaly/temporal/TemporalAnomalyTest.java

public class TemporalAnomalyTest {
    
    @Test
    public void testTemporalAnomalyTick() {
        TemporalAnomaly anomaly = new TemporalAnomaly(0, 64, 0, 0.5D);
        
        assertEquals(0, anomaly.getAge());
        anomaly.tick();
        assertEquals(1, anomaly.getAge());
    }
    
    @Test
    public void testTemporalAnomalyIntensity() {
        TemporalAnomaly anomaly = new TemporalAnomaly(0, 64, 0, 0.5D);
        
        double expected = 1.0D + (0.5D * 3.0D); // 2.5x speed
        assertEquals(expected, anomaly.getTimeMultiplier(), 0.01D);
    }
}
```

## Extending the API

### Adding Public Methods

1. Add to `RDPAPI.java`:
```java
public static double getTemporalAnomalyCount(World world) {
    int count = 0;
    for (RDPRegion region : getWorldState(world).getAllRegions().values()) {
        for (Anomaly anomaly : region.getAnomalies().values()) {
            if ("TEMPORAL".equals(anomaly.getAnomalyType())) {
                count++;
            }
        }
    }
    return count;
}
```

2. Document in MODPACK_INTEGRATION_GUIDE.md

3. Add example usage

## Creating an Integration Adapter

### Template

```java
public class NewModIntegration extends ModIntegration {
    
    public NewModIntegration() {
        super("newmod");
    }
    
    @Override
    public boolean checkModLoaded() {
        try {
            Class.forName("com.example.newmod.NewMod");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
    
    @Override
    public void init() {
        if (checkModLoaded()) {
            log("NewMod detected. Initializing...");
            
            // Register listeners
            MinecraftForge.EVENT_BUS.register(new RDPNewModListener());
            
            log("NewMod integration ready.");
        }
    }
}

class RDPNewModListener {
    @SubscribeEvent
    public void onRDPStageChange(RDPStageChangeEvent event) {
        if (event.newStage == GlobalRDPLevel.RDPStage.RDPII) {
            // Trigger NewMod behavior
        }
    }
}
```

### Register in RDPIntegrationManager

```java
public class RDPIntegrationManager {
    
    public static void initializeIntegrations() {
        // Existing integrations...
        
        // Add new integration
        new NewModIntegration().init();
    }
}
```

## Testing on Modpack

### Preparation

1. Build: `gradlew.bat build`
2. Copy JAR to `mods/`
3. Start Minecraft

### Testing Checklist

- [ ] Mod loads without errors
- [ ] Config file created
- [ ] Events fire correctly
- [ ] API methods return correct values
- [ ] Persistence works (save/load)
- [ ] No console warnings
- [ ] Performance acceptable

### Debugging Commands

```
/rdp info                  # Show RDP state
/rdp setglobal 0.50      # Jump to RDP-II
/rdp region 0 0          # Show region data
```

## Performance Optimization

### Profiling

1. Use Minecraft's built-in profiler: `/profiler`
2. Enable JMH benchmarks for critical paths
3. Monitor memory usage with `/memory`

### Common Bottlenecks

- **Scanning all chunks**: Cache results, use spatial partitioning
- **Serialization**: Use efficient NBT encoding
- **Regional lookups**: Use hashmap, not linear search
- **Anomaly updates**: Batch operations

### Optimization Patterns

```java
// BAD: Scans every chunk every tick
for (Chunk chunk : world.getLoadedChunks()) {
    // Process chunk
}

// GOOD: Only process regions with changes
for (RDPRegion region : changedRegions) {
    // Process region
}
```

## Common Issues & Solutions

### Issue: My integration doesn't load

**Solution**: Check that the target mod class name is correct:

```java
// Debug: Print what class you're looking for
try {
    Class.forName("com.example.WrongClass");
} catch (ClassNotFoundException e) {
    System.out.println("Class not found: " + e);
}
```

### Issue: RDP level isn't increasing

**Solution**: Check config:
- `enable_rdp_progression=true`
- `global_rdp_increment_per_tick` > 0
- Check server logs

### Issue: Anomalies aren't spawning

**Solution**:
- Check `enableTemporalAnomalies=true`
- Verify RDP >= threshold
- Check region pressure > 0

### Issue: Memory leak

**Solution**:
- Ensure anomalies are removed when inactive
- Unload regions properly
- Check for circular references in NBT

## Contributing Code

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/my-feature`
3. Write code and tests
4. Update documentation
5. Submit a pull request

### Code Style

- Follow existing indentation (4 spaces)
- Use descriptive variable names
- Add Javadoc for public methods
- Keep methods under 50 lines
- Comment complex logic

### Commit Messages

```
[module] Brief description

Longer explanation of changes.
Fixes #123
```

## Documentation Standards

All public APIs must be documented:

```java
/**
 * Brief description of what this does.
 * 
 * Longer explanation if needed. Can span multiple lines.
 * 
 * @param world The Minecraft world
 * @param chunkX X coordinate of chunk
 * @param chunkZ Z coordinate of chunk
 * @return The regional RDP level (0.0 to 1.0)
 * 
 * @see RDPAPI#getGlobalRDPLevel(World)
 */
public static double getRegionalRDPLevel(World world, int chunkX, int chunkZ) {
    // Implementation
}
```

## Release Checklist

- [ ] All tests pass
- [ ] Code reviewed
- [ ] Documentation updated
- [ ] Version bumped
- [ ] Changelog updated
- [ ] Build successful
- [ ] JAR tested on modpack
- [ ] No performance regressions
- ] No new console warnings

---

**Document Version**: 1.0  
**Last Updated**: 2026-08-27
