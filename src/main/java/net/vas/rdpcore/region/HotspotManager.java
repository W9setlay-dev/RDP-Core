package net.vas.rdpcore.region;

import net.vas.rdpcore.world.RDPWorldState;
import net.vas.rdpcore.region.Hotspot;
import net.vas.rdpcore.region.RDPRegion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Iterator;
import java.util.Map;

/**
 * Manages hotspots lifecycle: growth, decay, merging, and pressure contribution.
 */
public class HotspotManager {

    private static final Logger LOGGER = LogManager.getLogger("rdpcore");

    public static void tickWorld(RDPWorldState state) {
        if (state == null) return;
        Iterator<Map.Entry<String, Hotspot>> it = state.getHotspots().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Hotspot> e = it.next();
            Hotspot h = e.getValue();
            // simple growth/decay rules
            h.age++;
            // grow or decay depending on intensity sign
            h.intensity = Math.max(0.0D, h.intensity - 0.001D);
            if (h.intensity <= 0.001D) {
                it.remove();
                LOGGER.debug("Hotspot {} died", e.getKey());
            }
        }

        // merging pass (naive O(n^2) but hotspots are capped)
        for (Map.Entry<String, Hotspot> a : state.getHotspots().entrySet()) {
            for (Map.Entry<String, Hotspot> b : state.getHotspots().entrySet()) {
                if (a.getKey().equals(b.getKey())) continue;
                Hotspot A = a.getValue();
                Hotspot B = b.getValue();
                double dx = A.x - B.x;
                double dz = A.z - B.z;
                double dist2 = dx*dx + dz*dz;
                double mergeDist = (A.radius + B.radius)*(A.radius + B.radius);
                if (dist2 <= mergeDist && A.intensity >= 0.01D && B.intensity >= 0.01D) {
                    // merge B into A
                    A.intensity = Math.min(1.0D, A.intensity + B.intensity * 0.7D);
                    A.radius = Math.max(A.radius, B.radius) * 1.1D;
                    state.getHotspots().remove(b.getKey());
                    LOGGER.debug("Merged hotspot {} into {}", b.getKey(), a.getKey());
                    break; // restart outer loop safely
                }
            }
        }

        // pressure contribution pass (apply to nearby regions)
        for (Map.Entry<String, Hotspot> e : state.getHotspots().entrySet()) {
            Hotspot h = e.getValue();
            // compute affected region coordinates
            int blocksPerRegion = net.vas.rdpcore.config.RDPConfig.REGION_SIZE_CHUNKS * 16;
            int regionX = h.x / blocksPerRegion;
            int regionZ = h.z / blocksPerRegion;
            RDPRegion r = state.getRegion(regionX, regionZ);
            if (r != null) {
                double contrib = h.intensity * 0.02D;
                r.addPressure(contrib);
            }
        }
    }

    public static String spawnHotspot(RDPWorldState state, int x, int y, int z, double radius, double intensity, String type) {
        if (state == null) return null;
        String id = "hotspot_" + x + "_" + z + "_" + System.currentTimeMillis();
        Hotspot h = new Hotspot(x, y, z, radius, intensity, type);
        state.getHotspots().put(id, h);
        return id;
    }
}
