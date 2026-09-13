package net.vas.rdpcore.cosmology;

/** Small allocation-free helpers shared by the state machine and renderer. */
public final class CosmologyMath {
    private CosmologyMath() { }
    public static double lerp(double a, double b, double t) { return a + (b - a) * Math.max(0.0D, Math.min(1.0D, t)); }
    public static double smooth(double a, double b, double t) { return lerp(a, b, Easing.SMOOTHSTEP.apply(t)); }
    public static long stableSeed(long worldSeed, int dimension, String event, String object) {
        long h = 1125899906842597L ^ worldSeed ^ (long) dimension * 31L;
        h = 31L * h + (event == null ? 0 : event.hashCode());
        return 31L * h + (object == null ? 0 : object.hashCode());
    }
}
