package net.vas.rdpcore.api;

import net.minecraft.world.World;
import net.minecraft.server.MinecraftServer;
import net.vas.rdpcore.core.GlobalRDPLevel;
import net.vas.rdpcore.region.RDPRegion;
import net.vas.rdpcore.world.RDPWorldState;
import net.vas.rdpcore.entity.RealityAnchor;
import net.vas.rdpcore.anomaly.Anomaly;
import net.vas.rdpcore.server.RDPServerContext;
import java.util.HashMap;
import java.util.Map;

/**
 * Public API surface for R.D.P. Core.
 * External mods can use these methods to interact with the RDP system.
 */
public class RDPAPI {
    
    private static final Map<String, RDPWorldState> worldStates = new HashMap<>();
    
    /**
     * Get the RDP server context (server lifecycle owner)
     */
    public static RDPServerContext getServerContext() {
        return RDPServerContext.getInstance();
    }
    
    /**
     * Get the active MinecraftServer instance
     */
    public static MinecraftServer getMinecraftServer() {
        RDPServerContext context = getServerContext();
        return context != null ? context.getServer() : null;
    }
    
    /**
     * Get the global RDP level for a world
     */
    public static double getGlobalRDPLevel(World world) {
        RDPWorldState state = getWorldState(world);
        return state != null ? state.getGlobalRDPLevel().getLevel() : 0.0D;
    }
    
    /**
     * Set the global RDP level
     */
    public static void setGlobalRDPLevel(World world, double level) {
        RDPWorldState state = getWorldState(world);
        if (state != null) {
            state.getGlobalRDPLevel().setLevel(level);
        }
    }
    
    /**
     * Increment global RDP level by a delta
     */
    public static void addGlobalRDP(World world, double delta) {
        RDPWorldState state = getWorldState(world);
        if (state != null) {
            state.getGlobalRDPLevel().addLevel(delta);
        }
    }
    
    /**
     * Get the RDP level for a specific region (based on chunk coordinates)
     */
    public static double getRegionalRDPLevel(World world, int chunkX, int chunkZ) {
        RDPWorldState state = getWorldState(world);
        if (state != null) {
            RDPRegion region = state.getOrCreateRegion(chunkX, chunkZ);
            return region.getLocalRDPLevel();
        }
        return 0.0D;
    }
    
    /**
     * Set the RDP level for a specific region
     */
    public static void setRegionalRDPLevel(World world, int chunkX, int chunkZ, double level) {
        RDPWorldState state = getWorldState(world);
        if (state != null) {
            RDPRegion region = state.getOrCreateRegion(chunkX, chunkZ);
            region.setLocalRDPLevel(level);
        }
    }
    
    /**
     * Add RDP to a region
     */
    public static void addRegionalRDP(World world, int chunkX, int chunkZ, double delta) {
        RDPWorldState state = getWorldState(world);
        if (state != null) {
            RDPRegion region = state.getOrCreateRegion(chunkX, chunkZ);
            region.addLocalRDP(delta);
        }
    }
    
    /**
     * Get the pressure in a region
     */
    public static double getRegionPressure(World world, int chunkX, int chunkZ) {
        RDPWorldState state = getWorldState(world);
        if (state != null) {
            RDPRegion region = state.getOrCreateRegion(chunkX, chunkZ);
            return region.getPressure();
        }
        return 0.0D;
    }
    
    /**
     * Add pressure to a region
     */
    public static void addRegionPressure(World world, int chunkX, int chunkZ, double delta) {
        RDPWorldState state = getWorldState(world);
        if (state != null) {
            RDPRegion region = state.getOrCreateRegion(chunkX, chunkZ);
            region.addPressure(delta);
        }
    }
    
    /**
     * Get the current global RDP stage
     */
    public static GlobalRDPLevel.RDPStage getCurrentStage(World world) {
        RDPWorldState state = getWorldState(world);
        if (state != null) {
            return state.getGlobalRDPLevel().getCurrentStage();
        }
        return GlobalRDPLevel.RDPStage.RDP0;
    }
    
    /**
     * Check if a specific RDP stage has been reached
     */
    public static boolean hasReachedStage(World world, GlobalRDPLevel.RDPStage stage) {
        RDPWorldState state = getWorldState(world);
        if (state != null) {
            return state.getGlobalRDPLevel().hasReachedStage(stage);
        }
        return false;
    }
    
    /**
     * Spawn an anomaly at a location
     */
    public static void spawnAnomaly(World world, int x, int y, int z, String anomalyType, double intensity) {
        // TODO: Create and register anomaly
        // This will be implemented in the anomaly registry
    }
    
    /**
     * Place a reality anchor
     */
    public static RealityAnchor placeRealityAnchor(World world, int x, int y, int z) {
        RealityAnchor anchor = new RealityAnchor(x, y, z, world.getWorldInfo().getWorldName());
        // TODO: Register anchor in world state
        return anchor;
    }
    
    /**
     * Check if Judgement Day is active
     */
    public static boolean isJudgementDayActive(World world) {
        RDPWorldState state = getWorldState(world);
        return state != null && state.isJudgementDayActive();
    }
    
    /**
     * Trigger Judgement Day
     */
    public static void triggerJudgementDay(World world) {
        RDPWorldState state = getWorldState(world);
        if (state != null) {
            state.setJudgementDayActive(true);
        }
    }
    
    /**
     * Get or initialize world state
     */
    public static RDPWorldState getWorldState(World world) {
        if (world == null) return null;
        
        String worldKey = world.getWorldInfo().getWorldName();
        if (!worldStates.containsKey(worldKey)) {
            worldStates.put(worldKey, new RDPWorldState(world));
        }
        return worldStates.get(worldKey);
    }
    
    /**
     * Save or register world state (called from simulation loop or world save)
     */
    public static void saveWorldState(World world, RDPWorldState state) {
        String worldKey = world.getWorldInfo().getWorldName();
        worldStates.put(worldKey, state);
        // persistence is handled by world save hooks (RDPWorldEventHandler)
    }

    /**
     * Register world state (called when world loads)
     */
    public static void registerWorldState(World world, RDPWorldState state) {
        String worldKey = world.getWorldInfo().getWorldName();
        worldStates.put(worldKey, state);
    }
    
    /**
     * Unregister world state (called when world unloads)
     */
    public static void unregisterWorldState(World world) {
        if (world == null) return;
        String worldKey = world.getWorldInfo().getWorldName();
        worldStates.remove(worldKey);
    }
}
