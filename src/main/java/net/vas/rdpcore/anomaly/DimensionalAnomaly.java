package net.vas.rdpcore.anomaly;

import net.minecraft.nbt.NBTTagCompound;

/**
 * Dimensional anomaly — leaks dimension characteristics. Keep server-side behavior safe.
 */
public class DimensionalAnomaly extends Anomaly {

    public DimensionalAnomaly() { super(); }

    public DimensionalAnomaly(int x, int y, int z, double intensity) {
        super(x, y, z, intensity);
        this.maxAge = 20 * 60 * 6; // longer-lived
    }

    @Override
    public String getAnomalyType() {
        return "DIMENSIONAL";
    }

    @Override
    public void applyEffect(net.vas.rdpcore.world.RDPWorldState worldState) {
        int blocksPerRegion = net.vas.rdpcore.config.RDPConfig.REGION_SIZE_CHUNKS * 16;
        int regionX = this.x / blocksPerRegion;
        int regionZ = this.z / blocksPerRegion;
        net.vas.rdpcore.region.RDPRegion region = worldState.getRegion(regionX, regionZ);
        if (region != null) {
            double contrib = Math.min(0.08D, this.intensity * 0.03D);
            region.addPressure(contrib);
            // schedule a larger mutation with small chance to reflect dimensional leakage
            java.util.Random r = new java.util.Random(worldState.getWorld().getSeed() ^ this.x ^ this.z ^ this.age);
            if (r.nextDouble() < 0.01D) {
                int centerChunkX = (this.x >> 4);
                int centerChunkZ = (this.z >> 4);
                net.vas.rdpcore.mutation.MutationRequest req = net.vas.rdpcore.mutation.MutationRequest.builder()
                    .center(centerChunkX, centerChunkZ)
                    .radius(3)
                    .profile("rdp_dimensional_leak")
                    .intensity((float)Math.min(1.0D, this.intensity))
                    .priority(40)
                    .budget(500)
                    .cause("RDP_DIMENSIONAL_ANOMALY")
                    .dimension(worldState.getWorld().provider.getDimensionType().getName())
                    .build();
                net.vas.rdpcore.mutation.MutationCoordinator.getInstance().queueMutation(req, 50);
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
