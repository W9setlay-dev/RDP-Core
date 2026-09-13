package net.vas.rdpcore.anomaly.interdimensional;

public enum GravityFalloff {
    LINEAR, QUADRATIC, SMOOTHSTEP, INVERSE_SQUARE_CLAMPED;

    public double apply(double distance, double radius) {
        if (!Double.isFinite(distance) || !Double.isFinite(radius) || radius <= 0.0D) return 0.0D;
        double t = Math.max(0.0D, Math.min(1.0D, distance / radius));
        double v;
        switch (this) {
            case LINEAR: v = 1.0D - t; break;
            case SMOOTHSTEP: v = 1.0D - (t * t * (3.0D - 2.0D * t)); break;
            case INVERSE_SQUARE_CLAMPED:
                v = 1.0D / (1.0D + 8.0D * t * t);
                break;
            case QUADRATIC:
            default: v = (1.0D - t) * (1.0D - t); break;
        }
        return Math.max(0.0D, Math.min(1.0D, v));
    }
}
