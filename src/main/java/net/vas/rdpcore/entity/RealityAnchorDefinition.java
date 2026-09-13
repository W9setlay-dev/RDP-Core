package net.vas.rdpcore.entity;

import java.util.EnumSet;
import java.util.Set;

public final class RealityAnchorDefinition {
    private final String blockId;
    private final int metadata;
    private final double radius;
    private final double strength;
    private final Set<RealityAnchorCapability> capabilities;
    private final String profile;

    public RealityAnchorDefinition(String blockId, int metadata, double radius, double strength,
                                   Set<RealityAnchorCapability> capabilities) {
        this(blockId, metadata, radius, strength, capabilities, "default");
    }

    public RealityAnchorDefinition(String blockId, int metadata, double radius, double strength,
                                   Set<RealityAnchorCapability> capabilities, String profile) {
        if (blockId == null || blockId.trim().isEmpty()) {
            throw new IllegalArgumentException("blockId must not be empty");
        }
        this.blockId = blockId;
        this.metadata = metadata;
        RealityAnchorProfile resolved = RealityAnchorProfiles.find(profile);
        if (resolved == null) throw new IllegalArgumentException("unknown anchor profile: " + profile);
        this.radius = Math.max(0.0D, radius * resolved.getRadiusMultiplier());
        this.strength = Math.max(0.0D, Math.min(1.0D, strength * resolved.getStrengthMultiplier()));
        EnumSet<RealityAnchorCapability> merged = capabilities == null || capabilities.isEmpty()
            ? EnumSet.of(RealityAnchorCapability.REALITY_STABILIZATION)
            : EnumSet.copyOf(capabilities);
        merged.addAll(resolved.getCapabilities());
        this.capabilities = merged;
        this.profile = profile == null || profile.trim().isEmpty() ? "default" : profile.trim();
    }

    public String getBlockId() { return blockId; }
    public int getMetadata() { return metadata; }
    public double getRadius() { return radius; }
    public double getStrength() { return strength; }
    public Set<RealityAnchorCapability> getCapabilities() { return EnumSet.copyOf(capabilities); }
    public boolean hasCapability(RealityAnchorCapability capability) { return capabilities.contains(capability); }
    public String getProfile() { return profile; }

    /** Returns a diagnostic rather than throwing, suitable for config validation. */
    public static String validate(String blockId, int metadata, double radius, double strength) {
        if (blockId == null || blockId.trim().isEmpty()) return "block id is empty";
        String id = blockId.trim();
        if (!id.matches("[a-z0-9_.-]+:[a-z0-9/_.-]+")) return "block id must be a namespaced registry id";
        if (metadata < -1) return "metadata must be -1 (wildcard) or non-negative";
        if (Double.isNaN(radius) || Double.isInfinite(radius) || radius < 0.0D) return "radius must be finite and non-negative";
        if (Double.isNaN(strength) || Double.isInfinite(strength) || strength < 0.0D || strength > 1.0D) return "strength must be between 0 and 1";
        return null;
    }
}
