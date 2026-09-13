package net.vas.rdpcore.anomaly.interdimensional;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Validated, server-side defaults for the local anomaly gravity field. */
public final class GravityFieldConfig {
    public boolean enabled = true;
    public Set<String> allowedProfiles = new HashSet<String>();
    public Set<String> deniedProfiles = new HashSet<String>();
    public Set<Integer> allowedHostDimensions = new HashSet<Integer>();
    public Set<Integer> allowedSourceDimensions = new HashSet<Integer>();
    public double minimumGlobalRdpLevel = 0.0D, maximumGlobalRdpLevel = 1.0D;
    public Set<String> requiredRdpStages = new HashSet<String>();
    public Set<String> forbiddenRdpStages = new HashSet<String>();
    public int minimumAnomalyStage = 1, maximumAnomalyStage = 3;
    public double minimumPressure = 0.20D, maximumPressure = 1.0D;
    public boolean pressureScaling = true;
    public GravityFalloff pressureCurve = GravityFalloff.SMOOTHSTEP;
    public long activationDelayTicks = 0L;
    public boolean buildupEnabled = true;
    public long buildupTicks = 1200L;
    public boolean disableDuringStabilization = true;
    public double decayMultiplier = 0.5D;
    public GravityMode mode = GravityMode.RADIAL_PLUS_DOWNWARD;
    public double strength = 0.08D;
    public double directionX = 0.0D, directionY = -1.0D, directionZ = 0.0D;
    public GravityFalloff falloff = GravityFalloff.QUADRATIC;
    public double radiusMultiplier = 1.0D, minimumRadius = 0.0D, maximumRadius = 128.0D;
    public double edgeStrength = 0.0D, centerMultiplier = 1.0D;
    public boolean rotationEnabled = true;
    public double rotationStrength = 0.35D;
    public boolean coreEnabled = true;
    public double coreRadius = 4.0D, coreMultiplier = 2.5D;
    public boolean orbitEnabled = false;
    public double orbitStrength = 0.10D, orbitMaxSpeed = 1.5D;
    public double maximumAcceleration = 0.25D;
    public double maximumVelocityChangePerTick = 0.30D;
    public double maximumHorizontalVelocity = 3.0D, maximumVerticalVelocity = 3.0D;
    public boolean affectPlayers = true, affectLivingEntities = true, affectItems = true;
    public boolean affectProjectiles = true, affectFallingBlocks = true, affectVehicles = false;
    public boolean affectCreativePlayers = false, affectSpectators = false;
    public Map<String, Double> entityMultipliers = new HashMap<String, Double>();
    public Set<String> excludedEntities = new HashSet<String>();
    public int maxAnomaliesProcessedPerTick = 16, maxEntitiesProcessedPerTick = 128;
    public int updateIntervalTicks = 1;

    public static GravityFieldConfig defaults() { return new GravityFieldConfig(); }

    public void validate() {
        minimumGlobalRdpLevel = clamp(minimumGlobalRdpLevel, 0, 1);
        maximumGlobalRdpLevel = clamp(maximumGlobalRdpLevel, minimumGlobalRdpLevel, 1);
        minimumPressure = clamp(minimumPressure, 0, 1);
        maximumPressure = clamp(maximumPressure, minimumPressure, 1);
        minimumAnomalyStage = Math.max(0, minimumAnomalyStage);
        maximumAnomalyStage = Math.max(minimumAnomalyStage, maximumAnomalyStage);
        activationDelayTicks = Math.max(0L, activationDelayTicks);
        buildupTicks = Math.max(0L, buildupTicks);
        strength = clampFinite(strength, 0, 1);
        radiusMultiplier = clampFinite(radiusMultiplier, 0, 16);
        minimumRadius = clampFinite(minimumRadius, 0, 4096);
        maximumRadius = clampFinite(maximumRadius, minimumRadius, 4096);
        rotationStrength = clampFinite(rotationStrength, 0, 1);
        orbitStrength = clampFinite(orbitStrength, 0, 1);
        orbitMaxSpeed = clampFinite(orbitMaxSpeed, 0, 3);
        maximumAcceleration = clampFinite(maximumAcceleration, 0, 0.25);
        maximumVelocityChangePerTick = clampFinite(maximumVelocityChangePerTick, 0, 0.30);
        maximumHorizontalVelocity = clampFinite(maximumHorizontalVelocity, 0, 3);
        maximumVerticalVelocity = clampFinite(maximumVerticalVelocity, 0, 3);
        maxAnomaliesProcessedPerTick = Math.max(0, Math.min(1024, maxAnomaliesProcessedPerTick));
        maxEntitiesProcessedPerTick = Math.max(0, Math.min(8192, maxEntitiesProcessedPerTick));
        updateIntervalTicks = Math.max(1, Math.min(20, updateIntervalTicks));
        Map<String, Double> clean = new HashMap<String, Double>();
        for (Map.Entry<String, Double> e : entityMultipliers.entrySet())
            if (e.getKey() != null && e.getValue() != null && Double.isFinite(e.getValue()))
                clean.put(e.getKey(), clamp(e.getValue(), 0, 4));
        entityMultipliers = clean;
    }

    public Set<String> getAllowedProfiles() { return Collections.unmodifiableSet(allowedProfiles); }
    private static double clamp(double v, double lo, double hi) {
        return Double.isFinite(v) ? Math.max(lo, Math.min(hi, v)) : lo;
    }
    private static double clampFinite(double v, double lo, double hi) { return clamp(v, lo, hi); }
}
