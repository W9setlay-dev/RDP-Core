package net.vas.rdpcore.anomaly.temporal;

import net.vas.rdpcore.anomaly.Anomaly;

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
    public void applyEffect(net.vas.rdpcore.world.RDPWorldState worldState) {
        // TODO: Apply temporal distortion effects
        // - Age entities differently
        // - Accelerate/decelerate processes
        // - Create time loops in small areas
    }
    
    @Override
    public void tick() {
        super.tick();
        // Temporal anomalies' time distortion changes based on intensity
        this.timeMultiplier = 1.0D + (intensity * 3.0D); // 1.0x to 4.0x speed
    }
}
