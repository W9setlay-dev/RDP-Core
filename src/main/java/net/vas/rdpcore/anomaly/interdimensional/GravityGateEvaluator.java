package net.vas.rdpcore.anomaly.interdimensional;

import net.vas.rdpcore.core.GlobalRDPLevel;

public final class GravityGateEvaluator {
    private GravityGateEvaluator() { }

    public static GravityGateResult evaluate(InterdimensionalAnomaly anomaly,
                                              GravityFieldConfig config, double rdpLevel,
                                              GlobalRDPLevel.RDPStage rdpStage, long tick) {
        if (anomaly == null || config == null) return GravityGateResult.inactive(GravityGateResult.Reason.INACTIVE_ANOMALY_STATE);
        config.validate();
        if (!config.enabled) return GravityGateResult.inactive(GravityGateResult.Reason.INACTIVE_DISABLED);
        if (!config.allowedProfiles.isEmpty() && !config.allowedProfiles.contains(anomaly.getProfileId()))
            return GravityGateResult.inactive(GravityGateResult.Reason.INACTIVE_PROFILE);
        if (config.deniedProfiles.contains(anomaly.getProfileId()))
            return GravityGateResult.inactive(GravityGateResult.Reason.INACTIVE_PROFILE);
        if (!config.allowedHostDimensions.isEmpty() && !config.allowedHostDimensions.contains(anomaly.getHostDimension()))
            return GravityGateResult.inactive(GravityGateResult.Reason.INACTIVE_DIMENSION);
        if (!config.allowedSourceDimensions.isEmpty() && !config.allowedSourceDimensions.contains(anomaly.getSourceDimension()))
            return GravityGateResult.inactive(GravityGateResult.Reason.INACTIVE_DIMENSION);
        if (anomaly.isClosed() || anomaly.getLifecycle() == AnomalyLifecycle.DORMANT)
            return GravityGateResult.inactive(GravityGateResult.Reason.INACTIVE_ANOMALY_STATE);
        int stage = anomaly.getStage().ordinal();
        if (stage < config.minimumAnomalyStage || stage > config.maximumAnomalyStage)
            return GravityGateResult.inactive(GravityGateResult.Reason.INACTIVE_ANOMALY_STAGE);
        if (rdpLevel < config.minimumGlobalRdpLevel || rdpLevel > config.maximumGlobalRdpLevel)
            return GravityGateResult.inactive(GravityGateResult.Reason.INACTIVE_RDP_LEVEL);
        String stageName = rdpStage == null ? "" : rdpStage.name();
        if (!config.requiredRdpStages.isEmpty() && !config.requiredRdpStages.contains(stageName))
            return GravityGateResult.inactive(GravityGateResult.Reason.INACTIVE_RDP_STAGE);
        if (config.forbiddenRdpStages.contains(stageName))
            return GravityGateResult.inactive(GravityGateResult.Reason.INACTIVE_RDP_STAGE);
        if (anomaly.getPressure() < config.minimumPressure || anomaly.getPressure() > config.maximumPressure)
            return GravityGateResult.inactive(GravityGateResult.Reason.INACTIVE_PRESSURE);
        long age = Math.max(0L, tick - anomaly.getCreatedAt());
        if (age < config.activationDelayTicks)
            return GravityGateResult.inactive(GravityGateResult.Reason.INACTIVE_GRACE_PERIOD);
        if (anomaly.getLifecycle() == AnomalyLifecycle.COLLAPSING && config.disableDuringStabilization)
            return GravityGateResult.inactive(GravityGateResult.Reason.INACTIVE_DECAY);
        double influence = 1.0D;
        if (config.pressureScaling) {
            double p = (anomaly.getPressure() - config.minimumPressure)
                    / Math.max(1e-9D, config.maximumPressure - config.minimumPressure);
            influence *= config.pressureCurve.apply(1.0D - p, 1.0D); // curve input is inverted distance
        }
        if (config.buildupEnabled && config.buildupTicks > 0)
            influence *= Math.min(1.0D, (double)(age - config.activationDelayTicks) / config.buildupTicks);
        if (anomaly.getLifecycle() == AnomalyLifecycle.COLLAPSING)
            influence *= config.decayMultiplier;
        return GravityGateResult.active(influence);
    }
}
