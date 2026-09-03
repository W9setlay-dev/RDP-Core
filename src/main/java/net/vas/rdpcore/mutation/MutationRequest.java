package net.vas.rdpcore.mutation;

/**
 * Simple data holder for mutation requests to the Chunk Rewriter.
 * In production, this would integrate with RDPChunkRewriter's MutationRequest class.
 */
public class MutationRequest {
    
    private final int centerChunkX;
    private final int centerChunkZ;
    private final int radius;
    private final String profile;
    private final float intensity;
    private final int priority;
    private final int budget;
    private final String cause;
    private final String dimension;
    
    private MutationRequest(Builder builder) {
        this.centerChunkX = builder.centerChunkX;
        this.centerChunkZ = builder.centerChunkZ;
        this.radius = builder.radius;
        this.profile = builder.profile;
        this.intensity = builder.intensity;
        this.priority = builder.priority;
        this.budget = builder.budget;
        this.cause = builder.cause;
        this.dimension = builder.dimension;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public int getCenterChunkX() {
        return centerChunkX;
    }
    
    public int getCenterChunkZ() {
        return centerChunkZ;
    }
    
    public int getRadius() {
        return radius;
    }
    
    public String getProfile() {
        return profile;
    }
    
    public float getIntensity() {
        return intensity;
    }
    
    public int getPriority() {
        return priority;
    }
    
    public int getBudget() {
        return budget;
    }
    
    public String getCause() {
        return cause;
    }
    
    public String getDimension() {
        return dimension;
    }
    
    public static class Builder {
        private int centerChunkX;
        private int centerChunkZ;
        private int radius = 0;
        private String profile = "rdp_default";
        private float intensity = 0.5F;
        private int priority = 50;
        private int budget = 1000;
        private String cause = "UNKNOWN";
        private String dimension = "minecraft:overworld";
        
        public Builder center(int chunkX, int chunkZ) {
            this.centerChunkX = chunkX;
            this.centerChunkZ = chunkZ;
            return this;
        }
        
        public Builder radius(int r) {
            this.radius = Math.max(0, r);
            return this;
        }
        
        public Builder profile(String p) {
            this.profile = p;
            return this;
        }
        
        public Builder intensity(float i) {
            this.intensity = Math.max(0.0F, Math.min(1.0F, i));
            return this;
        }
        
        public Builder priority(int p) {
            this.priority = p;
            return this;
        }
        
        public Builder budget(int b) {
            this.budget = Math.max(0, b);
            return this;
        }
        
        public Builder cause(String c) {
            this.cause = c;
            return this;
        }
        
        public Builder dimension(String d) {
            this.dimension = d;
            return this;
        }
        
        public MutationRequest build() {
            return new MutationRequest(this);
        }
    }
}
