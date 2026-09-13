package net.vas.rdpcore.entity;

import java.util.EnumSet;
import java.util.Set;

/** Resolved behavior shared by anchor definitions using the same profile. */
public final class RealityAnchorProfile {
    private final String name;
    private final double radiusMultiplier;
    private final double strengthMultiplier;
    private final Set<RealityAnchorCapability> capabilities;

    public RealityAnchorProfile(String name, double radiusMultiplier, double strengthMultiplier,
                                Set<RealityAnchorCapability> capabilities) {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("profile name is empty");
        if (radiusMultiplier < 0.0D || strengthMultiplier < 0.0D)
            throw new IllegalArgumentException("profile multipliers must be non-negative");
        this.name = name.trim();
        this.radiusMultiplier = radiusMultiplier;
        this.strengthMultiplier = strengthMultiplier;
        this.capabilities = capabilities == null || capabilities.isEmpty()
            ? EnumSet.noneOf(RealityAnchorCapability.class) : EnumSet.copyOf(capabilities);
    }

    public String getName() { return name; }
    public double getRadiusMultiplier() { return radiusMultiplier; }
    public double getStrengthMultiplier() { return strengthMultiplier; }
    public Set<RealityAnchorCapability> getCapabilities() { return EnumSet.copyOf(capabilities); }
}
