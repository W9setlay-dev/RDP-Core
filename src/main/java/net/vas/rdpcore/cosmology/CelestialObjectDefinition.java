package net.vas.rdpcore.cosmology;

import net.minecraft.util.ResourceLocation;

/**
 * Immutable, validated definition for one world-space celestial object.
 * Angles are degrees: azimuth is measured around the horizon and elevation
 * above it. Distance is only a depth representation; objects are camera-relative.
 */
public final class CelestialObjectDefinition {
    public final String id;
    public final ResourceLocation texture;
    public final boolean enabled, day, night;
    public final double azimuth, elevation, roll, angularSize, scale, brightness, alpha;
    public final double rotationSpeed, phase, phaseOrientation;
    public final double minRdp, maxRdp, minAnomalyPressure;
    public final int minDimension, maxDimension, maxDistance;
    public final long transitionTicks;
    public final Easing easing;

    private CelestialObjectDefinition(Builder b) {
        id = b.id; texture = b.texture; enabled = b.enabled; day = b.day; night = b.night;
        azimuth = b.azimuth; elevation = b.elevation; roll = b.roll; angularSize = b.angularSize;
        scale = b.scale; brightness = b.brightness; alpha = b.alpha; rotationSpeed = b.rotationSpeed;
        phase = b.phase; phaseOrientation = b.phaseOrientation; minRdp = b.minRdp; maxRdp = b.maxRdp;
        minAnomalyPressure = b.minAnomalyPressure; minDimension = b.minDimension; maxDimension = b.maxDimension;
        maxDistance = b.maxDistance; transitionTicks = b.transitionTicks; easing = b.easing;
    }

    public boolean validFor(double rdp, int dimension, boolean isDay, double anomalyPressure) {
        return enabled && rdp >= minRdp && rdp <= maxRdp && dimension >= minDimension
            && dimension <= maxDimension && anomalyPressure >= minAnomalyPressure && (isDay ? day : night);
    }

    public static Builder builder(String id, ResourceLocation texture) { return new Builder(id, texture); }
    public static final class Builder {
        private final String id; private final ResourceLocation texture;
        private boolean enabled = true, day = true, night = true;
        private double azimuth, elevation = 45.0D, roll, angularSize = 8.0D, scale = 1.0D;
        private double brightness = 1.0D, alpha = 1.0D, rotationSpeed, phase = 1.0D, phaseOrientation;
        private double minRdp, maxRdp = 1.0D, minAnomalyPressure;
        private int minDimension = Integer.MIN_VALUE, maxDimension = Integer.MAX_VALUE, maxDistance = 256;
        private long transitionTicks = 60L; private Easing easing = Easing.SMOOTHSTEP;
        private Builder(String id, ResourceLocation texture) {
            if (id == null || id.trim().isEmpty() || texture == null) throw new IllegalArgumentException("id and texture required");
            this.id = id; this.texture = texture;
        }
        public Builder enabled(boolean v) { enabled = v; return this; }
        public Builder day(boolean v) { day = v; return this; }
        public Builder night(boolean v) { night = v; return this; }
        public Builder position(double az, double el, double r) { azimuth = az; elevation = el; roll = r; return this; }
        public Builder size(double v) { angularSize = v; return this; }
        public Builder scale(double v) { scale = v; return this; }
        public Builder brightness(double v) { brightness = v; return this; }
        public Builder alpha(double v) { alpha = v; return this; }
        public Builder rotation(double v) { rotationSpeed = v; return this; }
        public Builder phase(double v) { phase = v; return this; }
        public Builder rdp(double min, double max) { minRdp = min; maxRdp = max; return this; }
        public Builder anomaly(double min) { minAnomalyPressure = min; return this; }
        public Builder dimensions(int min, int max) { minDimension = min; maxDimension = max; return this; }
        public Builder transition(long ticks, Easing e) { transitionTicks = Math.max(0L, ticks); easing = e == null ? Easing.SMOOTHSTEP : e; return this; }
        public CelestialObjectDefinition build() {
            if (!Double.isFinite(angularSize) || angularSize <= 0 || angularSize > 180
                || !Double.isFinite(scale) || scale <= 0 || !Double.isFinite(alpha) || alpha < 0 || alpha > 1
                || minRdp < 0 || maxRdp > 1 || minRdp > maxRdp) throw new IllegalArgumentException("invalid celestial bounds");
            return new CelestialObjectDefinition(this);
        }
    }
}
