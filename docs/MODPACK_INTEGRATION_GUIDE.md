# RDP Core Modpack Integration Guide

## Overview

R.D.P. Core provides a public API for other mods and modpack systems to integrate with the Reality Distortion Phenomenon framework. This guide shows how to use RDP Core in your own systems.

## Public API

### Accessing RDP State

```java
import net.vas.rdpcore.api.RDPAPI;
import net.vas.rdpcore.core.GlobalRDPLevel;

// Get current global RDP level (0.0 to 1.0)
double globalRDP = RDPAPI.getGlobalRDPLevel(world);

// Get regional RDP level
double regionalRDP = RDPAPI.getRegionalRDPLevel(world, chunkX, chunkZ);

// Get current RDP stage
GlobalRDPLevel.RDPStage stage = RDPAPI.getCurrentStage(world);

// Check if stage reached
if (RDPAPI.hasReachedStage(world, GlobalRDPLevel.RDPStage.RDPII)) {
    // RDP-II threshold reached
}
```

### Modifying RDP State

```java
// Increase global RDP
RDPAPI.addGlobalRDP(world, 0.01D);

// Set global RDP to specific value
RDPAPI.setGlobalRDPLevel(world, 0.50D);

// Add regional pressure (affects local anomalies)
RDPAPI.addRegionPressure(world, chunkX, chunkZ, 0.1D);

// Spawn an anomaly
RDPAPI.spawnAnomaly(world, x, y, z, "SPATIAL", 0.8D);
```

### Reality Anchors

```java
// Place a reality anchor
RealityAnchor anchor = RDPAPI.placeRealityAnchor(world, x, y, z);

// Damage an anchor (0.0 to 1.0)
anchor.damage(0.3D);

// Check if anchor is active
if (anchor.isActive()) {
    // Anchor is resisting RDP progression
}
```

## Integration Examples

### Example 1: Event-Driven Integration

Listen for RDP stage changes:

```java
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.vas.rdpcore.event.RDPStageChangeEvent;

public class MyRDPListener {
    
    @SubscribeEvent
    public void onStageChange(RDPStageChangeEvent event) {
        if (event.newStage == GlobalRDPLevel.RDPStage.RDPII) {
            // Custom behavior when RDP-II is reached
            System.out.println("RDP-II THRESHOLD REACHED!");
            // Trigger your own systems
        }
    }
}

// Register listener
@Mod.EventHandler
public void init(FMLInitializationEvent event) {
    MinecraftForge.EVENT_BUS.register(new MyRDPListener());
}
```

### Example 2: CraftTweaker Integration

Modify recipes based on RDP progression:

```javascript
// scripts/rdp_recipes.zs

events.onPlayerTick(function(event) {
    var world = event.player.world;
    
    // Example: Disable crafting of certain items above RDP-IV
    if (RDPCore.getGlobalRDP(world) >= 0.55) {
        // Block crafting
    }
});
```

### Example 3: InControl Integration

Modify mob spawning rules based on RDP:

```json
// config/incontrol/spawn.json

[
  {
    "name": "rdp_phase_2_mobs",
    "result": "deny",
    "dimension": "minecraft:overworld",
    "conditions": [
      "rnd=0.1"
    ],
    "comment": "Deny 10% of normal mobs in RDP-II"
  },
  {
    "name": "rdp_spawn_anomalous_mobs",
    "result": "allow",
    "dimension": "minecraft:overworld",
    "mob": "minecraft:zombie",
    "conditions": [
      "biome=*rdp*",
      "rnd=0.3"
    ]
  }
]
```

### Example 4: GameStages Integration

Require progression through RDP stages:

```javascript
// scripts/rdp_stages.zs

events.onRDPStageChange(function(event) {
    var stage = event.newStage;
    
    if (stage == "RDP-I") {
        Stages.addPlayer(event.player, "rdp_i");
    } else if (stage == "RDP-II") {
        Stages.addPlayer(event.player, "rdp_ii");
    }
    // ... etc
});
```

### Example 5: Custom Pressure System

Add pressure to regions based on external events:

```java
@SubscribeEvent
public void onEntitySpawn(EntityJoinWorldEvent event) {
    Entity entity = event.getEntity();
    World world = event.getWorld();
    
    // Anomalous entities add RDP pressure
    if (entity.hasCustomName() && entity.getName().contains("anomalous")) {
        int chunkX = entity.chunkCoordX;
        int chunkZ = entity.chunkCoordZ;
        
        // Add pressure to the region
        RDPAPI.addRegionPressure(world, chunkX, chunkZ, 0.05D);
    }
}
```

## Integration Patterns

### Pattern 1: Listen and React

```java
// Listen for RDP events
// Trigger your own behavior based on events

@SubscribeEvent
public void onJudgementDay(JudgementDayEvent event) {
    // World is entering chaos mode
    // Hide/disable safe structures
    // Spawn boss entities
}
```

### Pattern 2: Read and Adjust

```java
// Poll RDP state each tick
// Make dynamic adjustments based on level

@SubscribeEvent
public void onWorldTick(TickEvent.WorldTickEvent event) {
    double rdpLevel = RDPAPI.getGlobalRDPLevel(event.world);
    
    if (rdpLevel < 0.25D) {
        // Normal behavior
    } else if (rdpLevel < 0.55D) {
        // Increased anomalies
    } else {
        // Severe distortions
    }
}
```

### Pattern 3: Add Pressure

```java
// Certain events increase regional pressure
// This can trigger local mutations

@SubscribeEvent
public void onBoss Defeated(BossDefeatEvent event) {
    // Boss defeat releases reality pressure
    RDPAPI.addRegionPressure(
        event.world,
        event.bossX >> 4,
        event.bossZ >> 4,
        0.2D
    );
}
```

### Pattern 4: Reality Anchors

```java
// Player-built structures can act as anchors

@SubscribeEvent
public void onBlockPlace(BlockEvent.PlaceEvent event) {
    if (event.getState().getBlock() == myAnchorBlock) {
        RDPAPI.placeRealityAnchor(
            event.getWorld(),
            event.getPos().getX(),
            event.getPos().getY(),
            event.getPos().getZ()
        );
    }
}
```

## Advanced: Custom Integration Adapter

Create your own integration:

```java
import net.vas.rdpcore.integration.ModIntegration;

public class MyModIntegration extends ModIntegration {
    
    public MyModIntegration() {
        super("mymod");
    }
    
    @Override
    public boolean checkModLoaded() {
        try {
            Class.forName("com.example.MyMod");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
    
    @Override
    public void init() {
        if (checkModLoaded()) {
            log("MyMod detected. Initializing...");
            
            // Register listeners
            // Set up data bridges
            // Initialize custom state
            
            log("MyMod integration ready.");
        }
    }
    
    // Custom API for your integration
    public void doSomethingWithRDP(double rdpLevel) {
        if (!isLoaded) return;
        
        if (rdpLevel >= 0.50D) {
            // Custom behavior
        }
    }
}
```

Register your integration in RDPIntegrationManager.

## Troubleshooting Integration

### My events aren't firing

1. Verify you're registering the listener:
   ```java
   MinecraftForge.EVENT_BUS.register(this);
   ```

2. Check the event class name matches exactly

3. Ensure the method is public and has @SubscribeEvent

### API returns null

1. Verify RDP Core is loaded:
   ```java
   if (RDPAPI.getWorldState(world) == null) {
       // RDP not initialized for this world yet
   }
   ```

2. Ensure you're calling during the right phase (not pre-init)

### Integration mod not detected

1. Check the mod's class name is correct
2. Verify the mod is actually loaded
3. Check mod name in `/modlist`

## Performance Notes

- RDPAPI calls are generally O(1) or O(log n)
- Regional lookups use hashmap access
- Avoid polling every tick if not necessary
- Prefer event-driven approaches

## API Stability

This API is considered **STABLE** for:
- Reading global/regional RDP levels
- Listening to events
- Placing reality anchors

This API is **EXPERIMENTAL** for:
- Direct state manipulation
- Custom anomaly spawning
- Advanced integration patterns

## Future Extensions

Planned API additions:
- Mutation request system
- Anomaly queries
- Pressure source tracking
- Per-player RDP state (for multiplayer)
- Custom anomaly types registration
- Datapack-based configuration

---

**Version**: 1.0  
**Last Updated**: 2026-08-27
