package net.vas.rdpcore.anomaly;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * Spatial anomaly — displaces or folds space locally.
 * Server-side: affect region pressure, request small mutations, and log events.
 */
public class SpatialAnomaly extends Anomaly {

    public SpatialAnomaly() { super(); }

    public SpatialAnomaly(int x, int y, int z, double intensity) {
        super(x, y, z, intensity);
        this.maxAge = 20 * 60 * 5; // longer-lived
    }

    @Override
    public String getAnomalyType() {
        return "SPATIAL";
    }

    @Override
    public void applyEffect(net.vas.rdpcore.world.RDPWorldState worldState) {
        // Increase pressure in local region and possibly enqueue a small mutation
        int blocksPerRegion = net.vas.rdpcore.config.RDPConfig.REGION_SIZE_CHUNKS * 16;
        int regionX = this.x / blocksPerRegion;
        int regionZ = this.z / blocksPerRegion;
        net.vas.rdpcore.region.RDPRegion region = worldState.getRegion(regionX, regionZ);
        if (region != null) {
            double contrib = Math.min(0.05D, this.intensity * 0.02D);
            region.addPressure(contrib);
            // small chance to request a minor spatial mutation
            java.util.Random r = new java.util.Random(worldState.getWorld().getSeed() ^ this.x ^ this.z ^ this.age);
            if (r.nextDouble() < 0.02D) {
                int centerChunkX = (this.x >> 4);
                int centerChunkZ = (this.z >> 4);
                net.vas.rdpcore.mutation.MutationRequest req = net.vas.rdpcore.mutation.MutationRequest.builder()
                    .center(centerChunkX, centerChunkZ)
                    .radius(1)
                    .profile("rdp_spatial_micro")
                    .intensity((float)Math.min(1.0D, this.intensity))
                    .priority(10)
                    .budget(50)
                    .cause("RDP_SPATIAL_ANOMALY")
                    .dimension(worldState.getWorld().provider.getDimensionType().getName())
                    .build();
                net.vas.rdpcore.mutation.MutationCoordinator.getInstance().queueMutation(req, 5);
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
