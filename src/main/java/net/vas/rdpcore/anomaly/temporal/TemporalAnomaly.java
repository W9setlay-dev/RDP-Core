package net.vas.rdpcore.anomaly.temporal;

import net.vas.rdpcore.anomaly.Anomaly;
import net.vas.rdpcore.config.RDPConfig;
import net.vas.rdpcore.mutation.MutationCoordinator;
import net.vas.rdpcore.mutation.MutationRequest;
import net.vas.rdpcore.region.RDPRegion;
import net.vas.rdpcore.world.RDPWorldState;

/**
 * Temporal anomalies represent time-based distortions.
 * Effects: Accelerated/reversed time, age fluctuations, event loops
 */
public class TemporalAnomaly extends Anomaly {
    
    private double timeMultiplier = 1.0D; // 2.0 = 2x speed, 0.5 = half speed
    private int affectedEntities = 0;
    
    public TemporalAnomaly(int x, int y, int z, double intensity) {
        super(x, y, z, intensity);
        this.maxAge = 20 * 90; // 1.5 minutes
    }
    
    public double getTimeMultiplier() {
        return timeMultiplier;
    }
    
    public void setTimeMultiplier(double multiplier) {
        this.timeMultiplier = multiplier;
    }
    
    public int getAffectedEntityCount() {
        return affectedEntities;
    }
    
    @Override
    public String getAnomalyType() {
        return "TEMPORAL";
    }
    
    @Override
    public void applyEffect(RDPWorldState worldState) {
        if (worldState == null) return;

        RDPRegion region = regionFor(worldState);
        double normalizedIntensity = Math.min(1.0D, getIntensity());
        region.addPressure(Math.min(0.03D, normalizedIntensity * 0.015D));
        affectedEntities = Math.min(64, region.getAnomalies().size());

        if (age > 0 && age % 40 == 0) {
            String dimension = worldState.getWorld() == null ? "unknown"
                : Integer.toString(worldState.getWorld().provider.getDimension());
            MutationRequest request = MutationRequest.builder()
                .center(x >> 4, z >> 4)
                .radius(1)
                .profile("rdp_temporal_pulse")
                .intensity((float) normalizedIntensity)
                .priority(15)
                .budget(25)
                .cause("RDP_TEMPORAL_ANOMALY")
                .dimension(dimension)
                .build();
            MutationCoordinator.getInstance().queueMutation(request, 10);
            worldState.recordMutationEvent("ANOMALY", "RDP_TEMPORAL_ANOMALY",
                region.getRegionX(), region.getRegionZ(), normalizedIntensity, dimension);
        }
    }
    
    @Override
    public void tick() {
        super.tick();
        // Temporal anomalies' time distortion changes based on intensity
        this.timeMultiplier = 1.0D + (Math.min(1.0D, getIntensity()) * 3.0D);
    }

    private RDPRegion regionFor(RDPWorldState worldState) {
        int blocksPerRegion = RDPConfig.REGION_SIZE_CHUNKS * 16;
        int regionX = Math.floorDiv(x, blocksPerRegion);
        int regionZ = Math.floorDiv(z, blocksPerRegion);
        return worldState.getOrCreateRegion(regionX * RDPConfig.REGION_SIZE_CHUNKS,
            regionZ * RDPConfig.REGION_SIZE_CHUNKS);
    }
}
