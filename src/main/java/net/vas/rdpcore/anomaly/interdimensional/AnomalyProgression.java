package net.vas.rdpcore.anomaly.interdimensional;

public final class AnomalyProgression {
    private AnomalyProgression() { }

    public static void advance(InterdimensionalAnomaly anomaly, AnomalyProfile profile,
                               double externalPressure, long timestamp, AnomalyConfig config) {
        if (anomaly == null || profile == null) throw new IllegalArgumentException("anomaly and profile are required");
        if (!Double.isFinite(externalPressure)) throw new IllegalArgumentException("externalPressure must be finite");
        anomaly.applyPressure(profile.getPressureGain() + externalPressure - profile.getPressureDecay(),
                timestamp, config);
    }
}
