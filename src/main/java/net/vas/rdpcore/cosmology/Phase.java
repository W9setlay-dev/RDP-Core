package net.vas.rdpcore.cosmology;

/** Visual phase of a spherical celestial body. */
public enum Phase {
    FULL(1.0D), GIBBOUS(0.75D), HALF(0.5D), CRESCENT(0.25D),
    THIN_CRESCENT(0.10D), NEW(0.0D), CUSTOM(-1.0D);

    private final double illumination;
    Phase(double illumination) { this.illumination = illumination; }
    public double illumination() { return illumination; }
}
