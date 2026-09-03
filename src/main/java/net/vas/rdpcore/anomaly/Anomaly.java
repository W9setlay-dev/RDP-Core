package net.vas.rdpcore.anomaly;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;
import java.util.UUID;

/**
 * Base class for all anomalies (Spatial, Temporal, Dimensional).
 * Anomalies represent distortions in reality at specific locations.
 */
public abstract class Anomaly implements INBTSerializable<NBTTagCompound> {
    
    protected String id = UUID.randomUUID().toString();
    protected int x, y, z;
    protected double intensity; // 0.0 to 1.0, can exceed 1.0 for extreme cases
    protected int age = 0; // Ticks since creation
    protected int maxAge = 20 * 60; // Default 60 seconds
    protected boolean isActive = true;
    
    public Anomaly() {
    }
    
    public Anomaly(int x, int y, int z, double intensity) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.intensity = intensity;
    }
    
    public String getId() {
        return id;
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
    
    public int getZ() {
        return z;
    }
    
    public double getIntensity() {
        return Math.max(0.0D, intensity);
    }
    
    public void setIntensity(double intensity) {
        this.intensity = intensity;
    }
    
    public int getAge() {
        return age;
    }
    
    public int getMaxAge() {
        return maxAge;
    }
    
    public boolean isActive() {
        return isActive && age < maxAge;
    }
    
    /**
     * Called each tick to update anomaly state
     */
    public void tick() {
        age++;
        if (age >= maxAge) {
            isActive = false;
        }
        // Decay intensity over time
        intensity = Math.max(0.0D, intensity * 0.995D);
    }
    
    /**
     * Get the type of this anomaly (SPATIAL, TEMPORAL, DIMENSIONAL, etc.)
     */
    public abstract String getAnomalyType();
    
    /**
     * Called when this anomaly affects the world. Receives the world-level state so implementations can
     * apply region/pressure/mutation effects safely on the server thread.
     */
    public abstract void applyEffect(net.vas.rdpcore.world.RDPWorldState worldState);
    
    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("id", id);
        tag.setInteger("x", x);
        tag.setInteger("y", y);
        tag.setInteger("z", z);
        tag.setDouble("intensity", intensity);
        tag.setInteger("age", age);
        tag.setInteger("maxAge", maxAge);
        tag.setBoolean("isActive", isActive);
        tag.setString("type", getAnomalyType());
        return tag;
    }
    
    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        this.id = nbt.getString("id");
        this.x = nbt.getInteger("x");
        this.y = nbt.getInteger("y");
        this.z = nbt.getInteger("z");
        this.intensity = nbt.getDouble("intensity");
        this.age = nbt.getInteger("age");
        this.maxAge = nbt.getInteger("maxAge");
        this.isActive = nbt.getBoolean("isActive");
    }
}
