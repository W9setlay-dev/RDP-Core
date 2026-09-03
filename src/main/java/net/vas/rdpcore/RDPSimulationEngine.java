package net.vas.rdpcore;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.vas.rdpcore.region.RDPRegion;
import net.vas.rdpcore.world.RDPWorldState;
import net.vas.rdpcore.util.PressureRegistry;
import net.vas.rdpcore.config.RDPConfig;
import net.vas.rdpcore.api.RDPAPI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Simulation engine that drives R.D.P. world-state progression.
 * Handles global progression, regional updates, and event triggering.
 */
public class RDPSimulationEngine {
    
    private static final Logger LOGGER = LogManager.getLogger("rdpcore");

    public static void init() {
        LOGGER.info("Initializing R.D.P. Simulation Engine...");

        // register basic pressure source
        PressureRegistry.register(new net.vas.rdpcore.util.BasePressureSource(0.01D));
        // register player pressure
        PressureRegistry.register(new net.vas.rdpcore.util.PlayerPressureSource(0.05D));
        // register SRP pressure if available
        try {
            net.vas.rdpcore.util.SRPPressureSource srp = new net.vas.rdpcore.util.SRPPressureSource(0.2D);
            PressureRegistry.register(srp);
        } catch (Throwable t) {
            LOGGER.debug("SRP pressure source not registered: {}", t.getMessage());
        }
        // register SCP pressure if available
        try {
            net.vas.rdpcore.util.SCPPressureSource scp = new net.vas.rdpcore.util.SCPPressureSource(1.0D);
            PressureRegistry.register(scp);
        } catch (Throwable t) {
            LOGGER.debug("SCP pressure source not registered: {}", t.getMessage());
        }

        LOGGER.info("R.D.P. Simulation Engine initialized.");
    }

    /**
     * Run a single simulation cycle for the given world.
     */
    public static void runSimulationForWorld(WorldServer world) {
        long simStart = System.nanoTime();
        long now = System.currentTimeMillis();
        LOGGER.debug("Running RDP simulation for world {} at tick {}", world.provider.getDimensionType().getName(), world.getTotalWorldTime());

        RDPWorldState state = RDPAPI.getWorldState(world);
        if (state == null) return;

        // Stage modifiers for this simulation
        net.vas.rdpcore.core.GlobalRDPLevel.RDPStage currentStage = state.getGlobalRDPLevel().getCurrentStage();
        net.vas.rdpcore.config.RDPConfig.StageModifiers currentMods = net.vas.rdpcore.config.RDPConfig.getStageModifiers(currentStage);

        // 1) Determine active regions
        List<Long> activeKeys = new ArrayList<>();
        try {
            net.vas.rdpcore.world.RegionManager.get(world).scanForPlayerRegions();
            activeKeys = net.vas.rdpcore.world.RegionManager.get(world).getActiveRegionKeys();
        } catch (Throwable t) {
            LOGGER.warn("Failed to build active region list: {}", t.getMessage());
        }

        // If empty, fall back to a small subset of regions near spawn
        if (activeKeys.isEmpty()) {
            int spawnX = world.getSpawnPoint().getX() >> 4;
            int spawnZ = world.getSpawnPoint().getZ() >> 4;
            long key = RDPWorldState.getRegionKey(spawnX / RDPConfig.REGION_SIZE_CHUNKS, spawnZ / RDPConfig.REGION_SIZE_CHUNKS);
            activeKeys.add(key);
        }

        // 2) Hotspot updates
        long hotspotStart = System.nanoTime();
        try {
            net.vas.rdpcore.region.HotspotManager.tickWorld(state);
        } catch (Throwable t) {
            LOGGER.warn("Hotspot manager failed: {}", t.getMessage());
        }
        long hotspotMs = (System.nanoTime() - hotspotStart) / 1_000_000;

        // 3) Regional updates
        int processed = 0;
        long pressureNano = 0L;
        long anomalyNano = 0L;
        long eventNano = 0L;
        for (Long key : activeKeys) {
            if (processed++ > 2000) break; // safety cap
            int regionX = (int)(key >> 32);
            int regionZ = (int)(key.longValue());
            RDPRegion region = state.getRegion(regionX, regionZ);
            if (region == null) {
                region = state.getOrCreateRegion(regionX * RDPConfig.REGION_SIZE_CHUNKS, regionZ * RDPConfig.REGION_SIZE_CHUNKS);
            }

            // Collect pressure (local)
            long pStart = System.nanoTime();
            double pressure = 0.0D;
            try {
                pressure = PressureRegistry.collectPressure(world, region);
            } catch (Throwable t) {
                LOGGER.warn("Pressure collection failed: {}", t.getMessage());
            }
            pressureNano += (System.nanoTime() - pStart);

            // apply decay
            pressure = Math.max(0.0D, pressure - RDPConfig.REGION_PRESSURE_DECAY);
            region.setPressure(pressure);

            // simple local RDP evolution: influenced by pressure and anchors
            double anchorEffect = Math.max(0.0D, region.getRealityAnchorCount() * RDPConfig.REALITY_ANCHOR_RDP_RESISTANCE);
            double delta = (pressure * 0.001D) - anchorEffect - (0.0005D); // damping
            // clamp per-cycle change
            delta = Math.max(-0.01D, Math.min(0.02D, delta));
            region.addLocalRDP(delta);

            // Propagation: naive neighbor diffusion
            int rx = region.getRegionX();
            int rz = region.getRegionZ();
            for (int ox = -1; ox <= 1; ox++) {
                for (int oz = -1; oz <= 1; oz++) {
                    if (ox == 0 && oz == 0) continue;
                    RDPRegion n = state.getRegion(rx + ox, rz + oz);
                    if (n == null) continue;
                    double influence = (n.getLocalRDPLevel() - region.getLocalRDPLevel()) * 0.05D * RDPConfig.REGION_PRESSURE_DECAY;
                    region.addLocalRDP(influence);
                }
            }

            // Anomaly spawning: simple probability based on localRDP
            try {
                double local = region.getLocalRDPLevel();
                Random r = deterministicRandom(world.getSeed(), rx, rz, world.getTotalWorldTime());
                double chance = Math.min(1.0D, local * 0.08D * currentMods.anomalyMultiplier);
                if (r.nextDouble() < chance && region.getAnomalies().size() < RDPConfig.MAX_ANOMALIES_PER_REGION) {
                    String id = "anomaly_" + rx + "_" + rz + "_" + now + "_" + r.nextInt(10000);
                    RDPRegion.AnomalyData d = new RDPRegion.AnomalyData();
                    // pick type weighted by thresholds
                    if (local >= RDPConfig.DIMENSIONAL_ANOMALY_THRESHOLD) d.type = "DIMENSIONAL";
                    else if (local >= RDPConfig.TEMPORAL_ANOMALY_THRESHOLD) d.type = "TEMPORAL";
                    else d.type = "SPATIAL";
                    d.intensity = Math.min(1.0D, local);
                    d.age = 0;
                    region.addAnomaly(id, d);
                    
                    // Record anomaly spawn event to history
                    state.recordMutationEvent("ANOMALY", d.type + "_SPAWN", region.getRegionX(), region.getRegionZ(), 
                        d.intensity, world.provider.getDimensionType().getName());
                    
                    // Create scars from high-intensity anomalies
                    if (d.intensity >= 0.6D) {
                        String scarId = "scar_" + id;
                        net.vas.rdpcore.world.Scar.ScarType scarType = d.intensity >= 0.85D
                            ? net.vas.rdpcore.world.Scar.ScarType.DIMENSIONAL
                            : net.vas.rdpcore.world.Scar.ScarType.SPATIAL;
                        net.vas.rdpcore.world.Scar scar = new net.vas.rdpcore.world.Scar(
                            scarId, scarType, region.getRegionX(), region.getRegionZ(),
                            d.intensity * 0.3D, "ANOMALY_" + d.type);
                        state.addScar(scarId, scar);
                    }
                }
            } catch (Throwable t) {
                LOGGER.warn("Anomaly simulation failed: {}", t.getMessage());
            }

            // Run anomaly manager for region
            long aStart = System.nanoTime();
            try {
                net.vas.rdpcore.anomaly.AnomalyManager.tickRegion(state, region);
            } catch (Throwable t) {
                LOGGER.warn("Anomaly manager failed for region {}: {}", key, t.getMessage());
            }
            anomalyNano += (System.nanoTime() - aStart);

            // Event scheduling per region/world
            long eStart = System.nanoTime();
            try {
                net.vas.rdpcore.event.EventScheduler.tickWorld(state);
            } catch (Throwable t) {
                LOGGER.warn("Event scheduler failed: {}", t.getMessage());
            }
            eventNano += (System.nanoTime() - eStart);

            // Mutation planning: queue mutation requests if localRDP exceeds threshold
            try {
                if (region.getLocalRDPLevel() >= RDPConfig.CHUNK_REWRITE_THRESHOLD && RDPConfig.ENABLE_CHUNK_REWRITING) {
                    int centerChunkX = region.getRegionX() * RDPConfig.REGION_SIZE_CHUNKS + (RDPConfig.REGION_SIZE_CHUNKS/2);
                    int centerChunkZ = region.getRegionZ() * RDPConfig.REGION_SIZE_CHUNKS + (RDPConfig.REGION_SIZE_CHUNKS/2);
                    net.vas.rdpcore.mutation.MutationRequest req = net.vas.rdpcore.mutation.MutationRequest.builder()
                        .center(centerChunkX, centerChunkZ)
                        .radius(RDPConfig.REGION_SIZE_CHUNKS)
                        .profile("rdp_regional_growth")
                        .intensity((float)Math.min(1.0D, region.getLocalRDPLevel()))
                        .priority(20)
                        .budget((int)(RDPConfig.MUTATION_NORMAL_BUDGET * currentMods.mutationMultiplier))
                        .cause("RDP_REGIONAL_GROWTH")
                        .dimension(world.provider.getDimensionType().getName())
                        .build();
                    net.vas.rdpcore.mutation.MutationCoordinator.getInstance().queueMutation(req, 10);
                    
                    // Record mutation event to history
                    state.recordMutationEvent("MUTATION", "RDP_REGIONAL_GROWTH", region.getRegionX(), region.getRegionZ(), 
                        region.getLocalRDPLevel(), world.provider.getDimensionType().getName());
                    
                    // Create scars from high-intensity mutations
                    if (region.getLocalRDPLevel() >= 0.7D) {
                        String scarId = "scar_" + region.getRegionX() + "_" + region.getRegionZ() + "_" + now + "_" + new Random().nextInt(10000);
                        net.vas.rdpcore.world.Scar.ScarType scarType = region.getLocalRDPLevel() >= 0.9D 
                            ? net.vas.rdpcore.world.Scar.ScarType.COSMOLOGICAL
                            : region.getLocalRDPLevel() >= 0.8D 
                            ? net.vas.rdpcore.world.Scar.ScarType.DIMENSIONAL
                            : net.vas.rdpcore.world.Scar.ScarType.SPATIAL;
                        net.vas.rdpcore.world.Scar scar = new net.vas.rdpcore.world.Scar(
                            scarId, scarType, region.getRegionX(), region.getRegionZ(), 
                            region.getLocalRDPLevel() * 0.5D, "MUTATION_INTENSITY");
                        state.addScar(scarId, scar);
                    }
                }
            } catch (Throwable t) {
                LOGGER.warn("Mutation planning failed: {}", t.getMessage());
            }
        }

        // Process queued mutation requests within the normal budget (scaled by stage)
        long mStart = System.nanoTime();
        int processedMutations = 0;
        try {
            int budget = Math.max(1, (int)(RDPConfig.CHUNK_REWRITE_BUDGET_PER_TICK * currentMods.mutationMultiplier));
            processedMutations = net.vas.rdpcore.mutation.MutationCoordinator.getInstance().processQueuedMutations(budget);
            if (processedMutations > 0) LOGGER.debug("Processed {} mutation requests (budget {})", processedMutations, budget);
        } catch (Throwable t) {
            LOGGER.warn("Mutation processing failed: {}", t.getMessage());
        }
        long mutationMs = (System.nanoTime() - mStart) / 1_000_000;

        // 4) Update global RDP based on average local levels, pressure, and scars
        try {
            double avg = 0.0D;
            int count = 0;
            for (RDPRegion r : state.getAllRegions().values()) {
                avg += r.getLocalRDPLevel();
                count++;
            }
            double avgLocal = count == 0 ? 0.0D : (avg / count);
            // Apply stage modifiers
            net.vas.rdpcore.core.GlobalRDPLevel.RDPStage stage = state.getGlobalRDPLevel().getCurrentStage();
            net.vas.rdpcore.config.RDPConfig.StageModifiers mods = net.vas.rdpcore.config.RDPConfig.getStageModifiers(stage);

            double pressureContribution = PressureRegistry.collectPressure(world, null) * 0.0001D * mods.pressureMultiplier;
            double scarPressure = state.getTotalScarPressure() * 0.0001D; // Scars contribute to pressure
            double baseGrowth = RDPConfig.GLOBAL_RDP_INCREMENT_PER_TICK * RDPConfig.SIMULATION_INTERVAL_TICKS * mods.mutationMultiplier;
            double growth = baseGrowth + (avgLocal * 0.01D * mods.anomalyMultiplier) + pressureContribution + scarPressure;
            double newLevel = Math.max(0.0D, Math.min(1.0D, state.getGlobalRDPLevel().getLevel() + growth));
            state.getGlobalRDPLevel().setLevel(newLevel);

            // Escalate scars based on current RDP level
            for (RDPRegion r : state.getAllRegions().values()) {
                r.escalateScars(newLevel);
            }

            // Stage transition event
            // Fire event if changed
            net.vas.rdpcore.event.RDPEvents.fireStageChangedIfNeeded(world, state.getGlobalRDPLevel());

        } catch (Throwable t) {
            LOGGER.warn("Global RDP update failed: {}", t.getMessage());
        }

        // 4) Persist dirty state occasionally
        try {
            // naive periodic save using world save hook
            if (world.getTotalWorldTime() % RDPConfig.RDP_SAVE_INTERVAL_TICKS == 0) {
                RDPAPI.saveWorldState(world, state);
            }
        } catch (Throwable t) {
            LOGGER.warn("RDP persistence failed: {}", t.getMessage());
        }

        long simMs = (System.nanoTime() - simStart) / 1_000_000;
        // record telemetry
        net.vas.rdpcore.util.Telemetry.recordSimulation(simMs, processed, pressureNano / 1_000_000, anomalyNano / 1_000_000, eventNano / 1_000_000, mutationMs, processedMutations);

        LOGGER.debug("RDP simulation finished for world {} ({} ms)", world.provider.getDimensionType().getName(), simMs);
    }

    private static Random deterministicRandom(long worldSeed, int regionX, int regionZ, long tick) {
        long seed = worldSeed ^ (((long) regionX << 32) ^ (regionZ & 0xFFFFFFFFL)) ^ tick;
        return new Random(seed);
    }
}
