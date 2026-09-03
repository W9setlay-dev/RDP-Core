package net.vas.rdpcore.anomaly.dimensional;

import net.vas.rdpcore.anomaly.Anomaly;

/**
 * Dimensional anomalies represent inter-dimensional distortions.
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
    public void applyEffect(net.vas.rdpcore.world.RDPWorldState worldState) {
        // TODO: Apply dimensional distortion effects
        // - Create dimensional rifts
        // - Spawn entities from other dimensions
        // - Allow blocks from other dimensions to leak through
    }
    
    @Override
    public void tick() {
        super.tick();
        // Dimensional rifts grow as intensity increases
        this.riftSize = (int) Math.ceil(1.0D + (intensity * 10.0D));
    }
}
