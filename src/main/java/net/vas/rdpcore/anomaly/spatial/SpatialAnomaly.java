package net.vas.rdpcore.anomaly.spatial;

import net.vas.rdpcore.anomaly.Anomaly;
import net.vas.rdpcore.config.RDPConfig;
import net.vas.rdpcore.mutation.MutationCoordinator;
import net.vas.rdpcore.mutation.MutationRequest;
import net.vas.rdpcore.region.RDPRegion;
import net.vas.rdpcore.world.RDPWorldState;

/**
 * Spatial anomalies represent geometric/positional distortions.
 * Effects: Blocks displaced, chunks misaligned, gravity anomalies
 */
public class SpatialAnomaly extends Anomaly {
    
    private double radiusBlocks = 32.0D;
    private int affectedChunks = 0;
    
    public SpatialAnomaly(int x, int y, int z, double intensity) {
        super(x, y, z, intensity);
        this.maxAge = 20 * 120; // 2 minutes
    }
    
    public double getRadius() {
        return radiusBlocks;
    }
    
    public void setRadius(double radius) {
        this.radiusBlocks = radius;
    }
    
    public int getAffectedChunkCount() {
        return affectedChunks;
    }
    
    @Override
    public String getAnomalyType() {
        return "SPATIAL";
    }
    
    @Override
    public void applyEffect(RDPWorldState worldState) {
        if (worldState == null) return;

        RDPRegion region = regionFor(worldState);
        double normalizedIntensity = Math.min(1.0D, getIntensity());
        region.addPressure(Math.min(0.05D, normalizedIntensity * 0.02D));
        affectedChunks = Math.min(256, (int) Math.ceil(Math.PI * radiusBlocks * radiusBlocks / 256.0D));

        if (age > 0 && age % 20 == 0) {
            String dimension = worldState.getWorld() == null ? "unknown"
                : Integer.toString(worldState.getWorld().provider.getDimension());
            MutationRequest request = MutationRequest.builder()
                .center(x >> 4, z >> 4)
                .radius(Math.min(4, Math.max(1, (int) Math.ceil(radiusBlocks / 16.0D))))
                .profile("rdp_spatial_micro")
                .intensity((float) normalizedIntensity)
                .priority(10)
                .budget(50)
                .cause("RDP_SPATIAL_ANOMALY")
                .dimension(dimension)
                .build();
            MutationCoordinator.getInstance().queueMutation(request, 5);
            worldState.recordMutationEvent("ANOMALY", "RDP_SPATIAL_ANOMALY",
                region.getRegionX(), region.getRegionZ(), normalizedIntensity, dimension);
        }
    }
    
    @Override
    public void tick() {
        super.tick();
        // Spatial anomalies expand/contract based on intensity
        this.radiusBlocks = Math.min(80.0D, 32.0D + (getIntensity() * 48.0D));
    }

    private RDPRegion regionFor(RDPWorldState worldState) {
        int blocksPerRegion = RDPConfig.REGION_SIZE_CHUNKS * 16;
        int regionX = Math.floorDiv(x, blocksPerRegion);
        int regionZ = Math.floorDiv(z, blocksPerRegion);
        return worldState.getOrCreateRegion(regionX * RDPConfig.REGION_SIZE_CHUNKS,
            regionZ * RDPConfig.REGION_SIZE_CHUNKS);
    }
}
