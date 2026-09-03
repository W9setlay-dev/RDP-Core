package net.vas.rdpcore.util;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;

/**
 * SRP (Scape and Run: Parasites) adapter pressure source.
 * Calculates pressure based on parasite entity count and infection spread.
 * Uses entity class name detection to count parasites without hard dependency.
 */
public class SRPPressureSource implements IRdpPressureSource {

    private final double weight;
    private int lastParasiteCount = 0;
    private long lastUpdateTick = 0;

    public SRPPressureSource(double weight) {
        this.weight = weight;
    }

    @Override
    public double getPressure(World world, Object context) {
        try {
            if (!Loader.isModLoaded("srparasites")) return 0.0D;
            
            // Cache parasite count for 20 ticks (performance optimization)
            long worldTick = world.getTotalWorldTime();
            if (worldTick - lastUpdateTick >= 20) {
                lastParasiteCount = countParasites(world);
                lastUpdateTick = worldTick;
            }
            
            // Pressure calculation:
            // 1. Entity-based: Count parasites (more = more pressure)
            // 2. RDP scaling: Higher RDP multiplies parasite pressure
            // 3. Evolution factor: Parasite diversity affects pressure
            
            double entityPressure = Math.min(1.0D, (lastParasiteCount / 100.0D) * weight);
            double global = net.vas.rdpcore.api.RDPAPI.getGlobalRDPLevel(world);
            double rdpScaling = global * (weight * 0.3D);
            
            // Combination with slight baseline for infected worlds
            return Math.min(1.0D, entityPressure + rdpScaling + 0.02D);
        } catch (Throwable t) {
            return 0.0D;
        }
    }
    
    /**
     * Count parasite entities in the world by checking entity class names
     */
    private int countParasites(World world) {
        int count = 0;
        try {
            for (Entity entity : world.loadedEntityList) {
                if (entity == null) continue;
                String className = entity.getClass().getName();
                String packageName = entity.getClass().getPackage() != null 
                    ? entity.getClass().getPackage().getName() 
                    : "";
                
                // Check for parasite entities from SRP mod
                if (packageName.contains("nex") 
                        || className.contains("Parasite")
                        || className.contains("Infection")
                        || className.contains("nex.entity")) {
                    count++;
                }
            }
        } catch (Throwable t) {
            // Silently fail, return cached value
        }
        return count;
    }
}
