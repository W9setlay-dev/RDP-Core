package net.vas.rdpcore.anomaly;

import net.minecraft.nbt.NBTTagCompound;
import net.vas.rdpcore.region.RDPRegion;

/**
 * Temporal anomaly — affects local temporal behavior (server-side: affect entity timers, events)
 */
public class TemporalAnomaly extends Anomaly {

    public TemporalAnomaly() { super(); }

    public TemporalAnomaly(int x, int y, int z, double intensity) {
        super(x, y, z, intensity);
        this.maxAge = 20 * 60 * 3; // medium-lived
    }

    @Override
    public String getAnomalyType() {
        return "TEMPORAL";
    }

    @Override
    public void applyEffect(net.vas.rdpcore.world.RDPWorldState worldState) {
        // Apply temporal pressure contribution and small event cooldown manipulations
        int blocksPerRegion = net.vas.rdpcore.config.RDPConfig.REGION_SIZE_CHUNKS * 16;
        int regionX = this.x / blocksPerRegion;
        int regionZ = this.z / blocksPerRegion;
        net.vas.rdpcore.region.RDPRegion region = worldState.getRegion(regionX, regionZ);
        if (region != null) {
            double contrib = Math.min(0.03D, this.intensity * 0.015D);
            region.addPressure(contrib);
            // increase anomaly intensity slightly
            for (String id : region.getAnomalies().keySet()) {
                RDPRegion.AnomalyData d = region.getAnomalies().get(id);
                d.intensity = Math.min(1.0D, d.intensity + 0.002D);
            }
        }
    }

    @Override
    public NBTTagCompound serializeNBT() {
        return super.serializeNBT();
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        super.deserializeNBT(nbt);
    }
}
