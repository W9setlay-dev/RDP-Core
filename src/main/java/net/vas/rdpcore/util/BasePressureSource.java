package net.vas.rdpcore.util;

import net.minecraft.world.World;

/**
 * Basic constant pressure provider to ensure minimal background pressure.
 */
public class BasePressureSource implements IRdpPressureSource {

    private final double base;

    public BasePressureSource(double base) {
        this.base = base;
    }

    @Override
    public double getPressure(World world, Object context) {
        return base;
    }

}
