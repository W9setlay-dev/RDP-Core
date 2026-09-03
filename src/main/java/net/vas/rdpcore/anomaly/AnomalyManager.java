package net.vas.rdpcore.anomaly;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.vas.rdpcore.region.RDPRegion;
import net.vas.rdpcore.world.RDPWorldState;
import net.vas.rdpcore.event.RDPEvents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Runtime manager for active anomaly instances. Persists only lightweight data in regions; materializes
 * Anomaly objects during simulation. Handles ticking, lifecycle, and removal.
 */
public class AnomalyManager {

    private static final Logger LOGGER = LogManager.getLogger("rdpcore");

    // worldName -> anomalyId -> Anomaly
    private static final Map<String, Map<String, Anomaly>> active = new ConcurrentHashMap<>();

    public static void ensureWorld(String worldName) {
        active.computeIfAbsent(worldName, k -> new ConcurrentHashMap<>());
    }

    public static void materializeFromRegion(RDPWorldState state, RDPRegion region) {
        String worldName = state.getWorld().getWorldInfo().getWorldName();
        ensureWorld(worldName);
        Map<String, Anomaly> map = active.get(worldName);
        for (Map.Entry<String, RDPRegion.AnomalyData> e : region.getAnomalies().entrySet()) {
            String id = e.getKey();
            if (map.containsKey(id)) continue;
            RDPRegion.AnomalyData d = e.getValue();
            Anomaly a = createFromData(d);
            if (a != null) {
                a.id = id;
                map.put(id, a);
                RDPEvents.post(new RDPEvents.AnomalySpawnEvent(a.getAnomalyType(), a.getX(), a.getY(), a.getZ(), a.getIntensity()));
                // If anomaly is strong, create a hotspot
                try {
                    if (a.getIntensity() >= 0.8D) {
                        net.vas.rdpcore.region.HotspotManager.spawnHotspot(state, a.getX(), a.getY(), a.getZ(), 32, a.getIntensity(), a.getAnomalyType());
                    }
                } catch (Throwable t) {
                    LOGGER.warn("Failed to spawn hotspot from anomaly {}: {}", id, t.getMessage());
                }
            }
        }
    }

    private static Anomaly createFromData(RDPRegion.AnomalyData d) {
        if (d == null || d.type == null) return null;
        switch (d.type.toUpperCase()) {
            case "SPATIAL":
                SpatialAnomaly s = new SpatialAnomaly(d.x, d.y, d.z, d.intensity);
                s.age = d.age;
                return s;
            case "TEMPORAL":
                TemporalAnomaly t = new TemporalAnomaly(d.x, d.y, d.z, d.intensity);
                t.age = d.age;
                return t;
            case "DIMENSIONAL":
                DimensionalAnomaly m = new DimensionalAnomaly(d.x, d.y, d.z, d.intensity);
                m.age = d.age;
                return m;
            default:
                return null;
        }
    }

    public static void tickRegion(RDPWorldState state, RDPRegion region) {
        String worldName = state.getWorld().getWorldInfo().getWorldName();
        ensureWorld(worldName);
        Map<String, Anomaly> map = active.get(worldName);

        // materialize any new anomalies from persisted region data
        materializeFromRegion(state, region);

        for (String id : map.keySet().toArray(new String[0])) {
            Anomaly a = map.get(id);
            if (a == null) continue;
            try {
                a.tick();
                a.applyEffect(state);
                if (!a.isActive()) {
                    map.remove(id);
                    region.removeAnomaly(id);
                    LOGGER.info("Anomaly {} expired and removed", id);
                } else {
                    // update backing data
                    if (region.getAnomalies().containsKey(id)) {
                        RDPRegion.AnomalyData d = region.getAnomalies().get(id);
                        d.age = a.getAge();
                        d.intensity = a.getIntensity();
                    }
                }
            } catch (Throwable t) {
                LOGGER.warn("Error ticking anomaly {}: {}", id, t.getMessage());
            }
        }
    }
}
