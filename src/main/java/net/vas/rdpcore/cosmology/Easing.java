package net.vas.rdpcore.cosmology;

public enum Easing {
    LINEAR, SMOOTHSTEP, EASE_IN, EASE_OUT, EASE_IN_OUT;

    public double apply(double value) {
        double t = Math.max(0.0D, Math.min(1.0D, value));
        switch (this) {
            case SMOOTHSTEP: return t * t * (3.0D - 2.0D * t);
            case EASE_IN: return t * t;
            case EASE_OUT: return 1.0D - (1.0D - t) * (1.0D - t);
            case EASE_IN_OUT: return t < 0.5D ? 2.0D * t * t : 1.0D - Math.pow(-2.0D * t + 2.0D, 2.0D) / 2.0D;
            default: return t;
        }
    }
}
