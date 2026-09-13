package net.vas.rdpcore.anomaly.interdimensional;

public final class AnomalyProfile {
    private final String id;
    private final int sourceDimension;
    private final int hostDimension;
    private final double initialRadius;
    private final double maximumRadius;
    private final double pressureGain;
    private final double pressureDecay;

    public AnomalyProfile(String id, int sourceDimension, int hostDimension, double initialRadius,
                          double maximumRadius, double pressureGain, double pressureDecay) {
        if (id == null || id.trim().isEmpty()) throw new IllegalArgumentException("profile id is required");
        if (!Double.isFinite(initialRadius) || initialRadius < 0.0D
                || !Double.isFinite(maximumRadius) || maximumRadius < initialRadius
                || !Double.isFinite(pressureGain) || pressureGain < 0.0D
                || !Double.isFinite(pressureDecay) || pressureDecay < 0.0D) {
            throw new IllegalArgumentException("invalid profile values");
        }
        this.id = id;
        this.sourceDimension = sourceDimension;
        this.hostDimension = hostDimension;
        this.initialRadius = initialRadius;
        this.maximumRadius = maximumRadius;
        this.pressureGain = pressureGain;
        this.pressureDecay = pressureDecay;
    }

    public String getId() { return id; }
    public int getSourceDimension() { return sourceDimension; }
    public int getHostDimension() { return hostDimension; }
    public double getInitialRadius() { return initialRadius; }
    public double getMaximumRadius() { return maximumRadius; }
    public double getPressureGain() { return pressureGain; }
    public double getPressureDecay() { return pressureDecay; }
}
