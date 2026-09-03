package net.vas.rdpcore.util;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;

/**
 * SCP adapter pressure source. Detects SCP entities and contributes pressure based on entity count.
 * Supports multiple SCP mods (SCP Project Anomalous, SCP-UA, etc.)
 */
public class SCPPressureSource implements IRdpPressureSource {

    private final double weight;
    private int lastEntityCount = 0;
    private long lastUpdateTick = 0;

    public SCPPressureSource(double weight) {
        this.weight = weight;
    }

    @Override
    public double getPressure(World world, Object context) {
        try {
            // Check if any SCP mod is loaded
            boolean scpLoaded = Loader.isModLoaded("scp_project_anomalous")
                    || Loader.isModLoaded("scp")
                    || Loader.isModLoaded("scpua")
                    || Loader.isModLoaded("zerascp")
                    || Loader.isModLoaded("scp001controller");
            
            if (!scpLoaded) return 0.0D;
            
            // Cache entity count for 20 ticks (performance optimization)
            long worldTick = world.getTotalWorldTime();
            if (worldTick - lastUpdateTick >= 20) {
                lastEntityCount = countSCPEntities(world);
                lastUpdateTick = worldTick;
            }
            
            // Pressure scales with SCP entity count and global RDP
            double global = net.vas.rdpcore.api.RDPAPI.getGlobalRDPLevel(world);
            double entityPressure = Math.min(1.0D, (lastEntityCount / 50.0D) * weight);
            double rdpScaling = global * (weight * 0.5D);
            
            return Math.min(1.0D, entityPressure + rdpScaling + 0.01D);
        } catch (Throwable t) {
            return 0.0D;
        }
    }
    
    /**
     * Count SCP entities in the world by checking entity class names
     */
    private int countSCPEntities(World world) {
        int count = 0;
        try {
            for (Entity entity : world.loadedEntityList) {
                String className = entity.getClass().getName();
                // Check for common SCP entity namespaces
                if (className.contains("scp") 
                        || className.contains("anomalous")
                        || className.contains("entity.scp")
                        || className.contains("SCP")) {
                    count++;
                }
            }
        } catch (Throwable t) {
            // Silently fail, return cached value
        }
        return count;
    }
}
