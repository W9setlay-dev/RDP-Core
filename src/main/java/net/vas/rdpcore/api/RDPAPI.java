package net.vas.rdpcore.api;

import net.minecraft.world.World;
import net.minecraft.server.MinecraftServer;
import net.vas.rdpcore.core.GlobalRDPLevel;
import net.vas.rdpcore.region.RDPRegion;
import net.vas.rdpcore.world.RDPWorldState;
import net.vas.rdpcore.entity.RealityAnchor;
import net.vas.rdpcore.entity.RealityAnchorDefinition;
import net.vas.rdpcore.entity.RealityAnchorRegistry;
import net.vas.rdpcore.entity.RealityAnchorCapability;
import net.vas.rdpcore.region.RDPRegion.AnomalyData;
import java.util.UUID;
import net.vas.rdpcore.anomaly.Anomaly;
import net.vas.rdpcore.server.RDPServerContext;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;
import java.util.UUID;
import net.vas.rdpcore.anomaly.interdimensional.InterdimensionalAnomaly;
import net.vas.rdpcore.anomaly.interdimensional.InterdimensionalAnomalyManager;
import net.vas.rdpcore.anomaly.interdimensional.GravityFieldConfig;
import net.vas.rdpcore.anomaly.interdimensional.GravityGateEvaluator;
import net.vas.rdpcore.anomaly.interdimensional.GravityGateResult;
import net.vas.rdpcore.config.RDPConfig;

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
        if (world == null || anomalyType == null || anomalyType.trim().isEmpty()) return;
        RDPWorldState state = getWorldState(world);
        if (state == null) return;
        RDPRegion region = state.getOrCreateRegion(Math.floorDiv(x, 16), Math.floorDiv(z, 16));
        if (region.getAnomalies().size() >= net.vas.rdpcore.config.RDPConfig.MAX_ANOMALIES_PER_REGION) return;
        AnomalyData data = new AnomalyData();
        data.type = anomalyType.toUpperCase();
        data.intensity = Math.max(0.0D, Math.min(1.0D, intensity));
        data.x = x;
        data.y = y;
        data.z = z;
        region.addAnomaly(UUID.randomUUID().toString(), data);
    }
    
    /**
     * Place a reality anchor
     */
    public static RealityAnchor placeRealityAnchor(World world, int x, int y, int z) {
        if (world == null) return null;
        RealityAnchor anchor = new RealityAnchor(x, y, z, world.getWorldInfo().getWorldName());
        RDPWorldState state = getWorldState(world);
        if (state != null) state.addRealityAnchor(UUID.randomUUID().toString(), anchor);
        return anchor;
    }

    public static RealityAnchor registerPlacedRealityAnchor(World world, int x, int y, int z,
                                                              String blockId, int metadata) {
        RDPWorldState state = getWorldState(world);
        return state == null ? null : state.registerPlacedAnchor(x, y, z, blockId, metadata);
    }

    public static RealityAnchor removePlacedRealityAnchor(World world, int x, int y, int z) {
        RDPWorldState state = getWorldState(world);
        return state == null ? null : state.removePlacedAnchor(x, y, z);
    }

    public static void registerRealityAnchorDefinition(RealityAnchorDefinition definition) {
        RealityAnchorRegistry.register(definition);
    }

    public static double getEffectiveRDP(World world, int x, int y, int z) {
        RDPWorldState state = getWorldState(world);
        if (state == null) return 0.0D;
        RDPRegion region = state.getOrCreateRegion(Math.floorDiv(x, 16), Math.floorDiv(z, 16));
        double base = Math.max(state.getGlobalRDPLevel().getLevel(), region.getLocalRDPLevel());
        return base * (1.0D - state.getAnchorSuppression(x, y, z,
            RealityAnchorCapability.REALITY_STABILIZATION));
    }

    public static double getAnchorInfluence(World world, int x, int y, int z,
                                             RealityAnchorCapability capability) {
        RDPWorldState state = getWorldState(world);
        return state == null ? 0.0D : state.getAnchorInfluence(x, y, z, capability);
    }
    
    /**
     * Check if Judgment Day is active
     */
    public static boolean isJudgementDayActive(World world) {
        RDPWorldState state = getWorldState(world);
        return state != null && state.isJudgementDayActive();
    }
    
    /**
     * Trigger Judgment Day
     */
    public static void triggerJudgementDay(World world) {
        RDPWorldState state = getWorldState(world);
        if (state != null) {
            state.setJudgementDayActive(true);
        }
    }

    public static InterdimensionalAnomaly createInterdimensionalAnomaly(World world,
                                                                             String profileId,
                                                                             double x, double y, double z) {
            if (!(world instanceof net.minecraft.world.WorldServer)) return null;
            return InterdimensionalAnomalyManager.create((net.minecraft.world.WorldServer) world,
                profileId, x, y, z, world.getTotalWorldTime());
    }

        public static Map<UUID, InterdimensionalAnomaly> getInterdimensionalAnomalies(World world) {
            RDPWorldState state = getWorldState(world);
            return state == null ? Collections.<UUID, InterdimensionalAnomaly>emptyMap()
                : state.getInterdimensionalAnomalies();
    }

        public static boolean removeInterdimensionalAnomaly(World world, UUID id) {
            RDPWorldState state = getWorldState(world);
            return state != null && state.removeInterdimensionalAnomaly(id) != null;
    }

    /** Read-only gravity gate diagnostics for server-side integrations. */
    public static GravityGateResult evaluateAnomalyGravity(World world, InterdimensionalAnomaly anomaly) {
        RDPWorldState state = getWorldState(world);
        return state == null ? GravityGateResult.inactive(GravityGateResult.Reason.INACTIVE_ANOMALY_STATE)
            : GravityGateEvaluator.evaluate(anomaly, RDPConfig.GRAVITY,
                state.getGlobalRDPLevel().getLevel(), state.getGlobalRDPLevel().getCurrentStage(),
                world.getTotalWorldTime());
    }
    
    /**
     * Get or initialize world state
     */
    public static RDPWorldState getWorldState(World world) {
        if (world == null) return null;
        
        String worldKey = world.getWorldInfo().getWorldName() + "#" + world.provider.getDimension();
        if (!worldStates.containsKey(worldKey)) {
            worldStates.put(worldKey, new RDPWorldState(world));
        }
        return worldStates.get(worldKey);
    }
    
    /**
     * Save or register world state (called from simulation loop or world save)
     */
    public static void saveWorldState(World world, RDPWorldState state) {
        String worldKey = world.getWorldInfo().getWorldName() + "#" + world.provider.getDimension();
        worldStates.put(worldKey, state);
        // persistence is handled by world save hooks (RDPWorldEventHandler)
    }

    /**
     * Register world state (called when world loads)
     */
    public static void registerWorldState(World world, RDPWorldState state) {
        String worldKey = world.getWorldInfo().getWorldName() + "#" + world.provider.getDimension();
        worldStates.put(worldKey, state);
    }
    
    /**
     * Unregister world state (called when world unloads)
     */
    public static void unregisterWorldState(World world) {
        if (world == null) return;
        String worldKey = world.getWorldInfo().getWorldName() + "#" + world.provider.getDimension();
        worldStates.remove(worldKey);
    }
}
