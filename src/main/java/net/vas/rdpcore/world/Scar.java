package net.vas.rdpcore.world;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * Represents a permanent scar in reality left by mutations and RDP escalation.
 * Scars are created when regions undergo significant distortions and persist indefinitely.
 * 
 * Scar Types:
 * - PHYSICAL: Block structure changes, geological distortions
 * - BIOLOGICAL: Mutation in flora/fauna, biological hybridization
 * - SPATIAL: Coordinate warping, dimensional pocket formation
 * - TEMPORAL: Time dilation zones, causality disruptions
 * - DIMENSIONAL: Dimensional bleed, reality layer tears
 * - COSMOLOGICAL: Fundamental reality breaks, irreversible changes
 */
public class Scar implements INBTSerializable<NBTTagCompound> {
    
    public enum ScarType {
        PHYSICAL(0.2D),       // Least severe
        BIOLOGICAL(0.3D),
        SPATIAL(0.4D),
        TEMPORAL(0.5D),
        DIMENSIONAL(0.7D),
        COSMOLOGICAL(1.0D);   // Most severe
        
        private final double pressureContribution;
        
        ScarType(double pressure) {
            this.pressureContribution = pressure;
        }
        
        public double getPressureContribution() {
            return pressureContribution;
        }
    }
    
    private String scarId;
    private ScarType type;
    private int regionX;
    private int regionZ;
    private double intensity;      // 0.0 to 1.0, indicates severity
    private long createdTick;      // World tick when created
    private long createdTime;      // System time when created
    private String cause;          // What caused this scar (anomaly, mutation, event, etc.)
    private double centerX, centerZ; // Approximate center in region
    
    public Scar() {
        // For deserialization
    }
    
    public Scar(String id, ScarType type, int regionX, int regionZ, double intensity, String cause) {
        this.scarId = id;
        this.type = type;
        this.regionX = regionX;
        this.regionZ = regionZ;
        this.intensity = Math.max(0.0D, Math.min(1.0D, intensity));
        this.cause = cause;
        this.createdTick = System.currentTimeMillis();
        this.createdTime = System.currentTimeMillis();
        this.centerX = regionX * 256.0D + 128.0D;
        this.centerZ = regionZ * 256.0D + 128.0D;
    }
    
    // Getters
    public String getScarId() { return scarId; }
    public ScarType getType() { return type; }
    public int getRegionX() { return regionX; }
    public int getRegionZ() { return regionZ; }
    public double getIntensity() { return intensity; }
    public long getCreatedTick() { return createdTick; }
    public long getCreatedTime() { return createdTime; }
    public String getCause() { return cause; }
    public double getCenterX() { return centerX; }
    public double getCenterZ() { return centerZ; }
    
    /**
     * Get pressure contribution of this scar
     * Higher intensity = higher pressure
     */
    public double getPressureContribution() {
        return type.getPressureContribution() * intensity;
    }
    
    /**
     * Scars can slowly expand/intensify over time at high RDP levels
     */
    public void escalate(double rdpLevel) {
        double escalationFactor = rdpLevel * 0.01D; // 0.01D per RDP level
        this.intensity = Math.min(1.0D, intensity + escalationFactor);
    }
    
    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("scarId", scarId);
        tag.setString("type", type.name());
        tag.setInteger("regionX", regionX);
        tag.setInteger("regionZ", regionZ);
        tag.setDouble("intensity", intensity);
        tag.setLong("createdTick", createdTick);
        tag.setLong("createdTime", createdTime);
        tag.setString("cause", cause);
        tag.setDouble("centerX", centerX);
        tag.setDouble("centerZ", centerZ);
        return tag;
    }
    
    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        this.scarId = nbt.getString("scarId");
        this.type = ScarType.valueOf(nbt.getString("type"));
        this.regionX = nbt.getInteger("regionX");
        this.regionZ = nbt.getInteger("regionZ");
        this.intensity = nbt.getDouble("intensity");
        this.createdTick = nbt.getLong("createdTick");
        this.createdTime = nbt.getLong("createdTime");
        this.cause = nbt.getString("cause");
        this.centerX = nbt.getDouble("centerX");
        this.centerZ = nbt.getDouble("centerZ");
    }
}
