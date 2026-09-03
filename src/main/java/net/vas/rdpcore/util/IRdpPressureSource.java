package net.vas.rdpcore.util;

import net.minecraft.world.World;

/**
 * Pressure source interface for RDP pressure providers.
 */
public interface IRdpPressureSource {

    /**
     * Calculate pressure contribution for the given world and simulation context.
     */
    double getPressure(World world, Object context);

    /**
     * Human-friendly id
     */
    default String getId() {
        return this.getClass().getSimpleName();
    }
}
