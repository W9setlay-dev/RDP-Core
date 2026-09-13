package net.vas.rdpcore.anomaly.interdimensional;

public final class AnomalyConfig {
    public static final double MAX_RADIUS_CAP = 4096.0D;
    public static final int MAX_AFFECTED_CHUNKS_CAP = 65536;
    public static final double MAX_PRESSURE_CAP = 1.0D;

    private final double maxRadius;
    private final int maxAffectedChunks;
    private final double activationPressure;
    private final double criticalPressure;
    private final double collapsePressure;

    public AnomalyConfig(double maxRadius, int maxAffectedChunks, double activationPressure,
                         double criticalPressure, double collapsePressure) {
        if (!Double.isFinite(maxRadius) || maxRadius <= 0.0D || maxRadius > MAX_RADIUS_CAP) {
            throw new IllegalArgumentException("maxRadius must be > 0 and <= " + MAX_RADIUS_CAP);
        }
        if (maxAffectedChunks <= 0 || maxAffectedChunks > MAX_AFFECTED_CHUNKS_CAP) {
            throw new IllegalArgumentException("maxAffectedChunks is outside the safety cap");
        }
        if (!validPressure(activationPressure) || !validPressure(criticalPressure)
                || !validPressure(collapsePressure)
                || activationPressure >= criticalPressure || criticalPressure >= collapsePressure) {
            throw new IllegalArgumentException("pressure thresholds must be ordered within [0, 1]");
        }
        this.maxRadius = maxRadius;
        this.maxAffectedChunks = maxAffectedChunks;
        this.activationPressure = activationPressure;
        this.criticalPressure = criticalPressure;
        this.collapsePressure = collapsePressure;
    }

    public static AnomalyConfig defaults() {
        return new AnomalyConfig(256.0D, 4096, 0.20D, 0.65D, 0.95D);
    }

    private static boolean validPressure(double value) {
        return Double.isFinite(value) && value >= 0.0D && value <= MAX_PRESSURE_CAP;
    }

    public double getMaxRadius() { return maxRadius; }
    public int getMaxAffectedChunks() { return maxAffectedChunks; }
    public double getActivationPressure() { return activationPressure; }
    public double getCriticalPressure() { return criticalPressure; }
    public double getCollapsePressure() { return collapsePressure; }
}
