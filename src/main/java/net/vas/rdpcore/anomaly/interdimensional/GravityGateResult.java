package net.vas.rdpcore.anomaly.interdimensional;

public final class GravityGateResult {
    public enum Reason {
        ACTIVE, INACTIVE_DISABLED, INACTIVE_PROFILE, INACTIVE_DIMENSION,
        INACTIVE_ANOMALY_STATE, INACTIVE_RDP_LEVEL, INACTIVE_RDP_STAGE,
        INACTIVE_ANOMALY_STAGE, INACTIVE_PRESSURE, INACTIVE_GRACE_PERIOD,
        INACTIVE_DECAY, INACTIVE_ENTITY
    }
    private final Reason reason;
    private final double influence;
    public GravityGateResult(Reason reason, double influence) {
        this.reason = reason;
        this.influence = Double.isFinite(influence) ? Math.max(0, Math.min(1, influence)) : 0;
    }
    public boolean isActive() { return reason == Reason.ACTIVE && influence > 0; }
    public Reason getReason() { return reason; }
    public double getInfluence() { return influence; }
    public static GravityGateResult inactive(Reason reason) { return new GravityGateResult(reason, 0); }
    public static GravityGateResult active(double influence) { return new GravityGateResult(Reason.ACTIVE, influence); }
}
