package net.vas.rdpcore.cosmology;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.vas.rdpcore.api.RDPAPI;
import net.vas.rdpcore.config.RDPConfig;

/** Common, side-safe profile registry and activation adapter. */
public final class CosmologyManager {
    private static final List<CelestialObjectDefinition> DEFINITIONS = new ArrayList<CelestialObjectDefinition>();
    private CosmologyManager() { }
    public static void init() {
        if (!DEFINITIONS.isEmpty()) return;
        try {
            ResourceLocation earth = new ResourceLocation("rdpcore", "cosmology/celestial/earth_anomaly.png");
            ResourceLocation galaxy = new ResourceLocation("rdpcore", "cosmology/galaxies/impossible_galaxy.png");
            DEFINITIONS.add(CelestialObjectDefinition.builder("earth_anomaly", earth)
                .position(35.0D, 38.0D, 0.0D).size(7.0D).scale(1.0D).brightness(1.15D)
                .phase(0.78D).rdp(0.10D, 1.0D).transition(60L, Easing.SMOOTHSTEP).build());
            DEFINITIONS.add(CelestialObjectDefinition.builder("impossible_galaxy", galaxy)
                .position(-115.0D, 35.0D, 0.0D).size(24.0D).scale(1.0D).brightness(0.65D)
                .alpha(0.6D).rdp(0.55D, 1.0D).transition(100L, Easing.EASE_IN_OUT).build());
        } catch (RuntimeException ignored) {
            // A malformed optional default must never prevent the game from loading.
        }
    }
    public static List<CelestialObjectDefinition> definitions() {
        init(); return Collections.unmodifiableList(DEFINITIONS);
    }
    public static List<CelestialObjectDefinition> active(World world, boolean day) {
        if (world == null || !RDPConfig.ENABLE_COSMOLOGY) return Collections.emptyList();
        double level = RDPAPI.getGlobalRDPLevel(world);
        double anomaly = 0.0D;
        try {
            for (net.vas.rdpcore.anomaly.interdimensional.InterdimensionalAnomaly a :
                    RDPAPI.getInterdimensionalAnomalies(world).values()) anomaly = Math.max(anomaly, a.getPressure());
        } catch (Throwable ignored) { }
        List<CelestialObjectDefinition> result = new ArrayList<CelestialObjectDefinition>();
        for (CelestialObjectDefinition d : definitions()) if (d.validFor(level, world.provider.getDimension(), day, anomaly)) result.add(d);
        return result;
    }
}
