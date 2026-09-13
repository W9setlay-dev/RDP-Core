package net.vas.rdpcore.anomaly.dimensional;

import net.vas.rdpcore.anomaly.Anomaly;
import net.vas.rdpcore.config.RDPConfig;
import net.vas.rdpcore.mutation.MutationCoordinator;
import net.vas.rdpcore.mutation.MutationRequest;
import net.vas.rdpcore.region.RDPRegion;
import net.vas.rdpcore.world.RDPWorldState;

/**
 * Dimensional anomalies represent interdimensional distortions.
 * Effects: Dimension rifts, entity spawning from other dimensions, dimensional bleeding
 */
public class DimensionalAnomaly extends Anomaly {
    
    private String linkedDimension = ""; // Dimension ID it's linked to
    private int riftSize = 1; // Radius of the dimensional rift
    private int spilledEntities = 0;
    
    public DimensionalAnomaly(int x, int y, int z, double intensity) {
        super(x, y, z, intensity);
        this.maxAge = 20 * 180; // 3 minutes
    }
    
    public String getLinkedDimension() {
        return linkedDimension;
    }
    
    public void setLinkedDimension(String dimensionId) {
        this.linkedDimension = dimensionId;
    }
    
    public int getRiftSize() {
        return riftSize;
    }
    
    public int getSpilledEntityCount() {
        return spilledEntities;
    }
    
    @Override
    public String getAnomalyType() {
        return "DIMENSIONAL";
    }
    
    @Override
    public void applyEffect(RDPWorldState worldState) {
        if (worldState == null) return;

        RDPRegion region = regionFor(worldState);
        double normalizedIntensity = Math.min(1.0D, getIntensity());
        region.addPressure(Math.min(0.08D, normalizedIntensity * 0.03D));
        spilledEntities = 0;

        if (age > 0 && age % 60 == 0) {
            String dimension = worldState.getWorld() == null ? "unknown"
                : Integer.toString(worldState.getWorld().provider.getDimension());
            MutationRequest request = MutationRequest.builder()
                .center(x >> 4, z >> 4)
                .radius(Math.min(4, riftSize))
                .profile("rdp_dimensional_leak")
                .intensity((float) normalizedIntensity)
                .priority(40)
                .budget(100)
                .cause("RDP_DIMENSIONAL_ANOMALY")
                .dimension(dimension)
                .build();
            MutationCoordinator.getInstance().queueMutation(request, 50);
            worldState.recordMutationEvent("ANOMALY", "RDP_DIMENSIONAL_ANOMALY",
                region.getRegionX(), region.getRegionZ(), normalizedIntensity, dimension);
        }
    }
    
    @Override
    public void tick() {
        super.tick();
        // Dimensional rifts grow as intensity increases
        this.riftSize = Math.min(10, Math.max(1, (int) Math.ceil(1.0D + (getIntensity() * 10.0D))));
    }

    private RDPRegion regionFor(RDPWorldState worldState) {
        int blocksPerRegion = RDPConfig.REGION_SIZE_CHUNKS * 16;
        int regionX = Math.floorDiv(x, blocksPerRegion);
        int regionZ = Math.floorDiv(z, blocksPerRegion);
        return worldState.getOrCreateRegion(regionX * RDPConfig.REGION_SIZE_CHUNKS,
            regionZ * RDPConfig.REGION_SIZE_CHUNKS);
    }
}
