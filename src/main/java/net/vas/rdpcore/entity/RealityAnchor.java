package net.vas.rdpcore.entity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * Reality Anchor - a manifestation or structure that resists RDP progression.
 * Can be:
 * - Player-built structures dedicated to reality stabilization
 * - Dimensional anchors placed by integration systems
 * - Natural formations that exhibit anchor properties
 */
public class RealityAnchor implements INBTSerializable<NBTTagCompound> {
    
    private int x, y, z;
    private String worldName;
    private double stabilityStrength = 1.0D; // How much RDP it resists
    private int age = 0;
    private int maxAge = Integer.MAX_VALUE; // Anchors persist until destroyed
    private boolean isActive = true;
    private double damageFraction = 0.0D; // 0.0 to 1.0, 1.0 = destroyed
    
    public RealityAnchor(int x, int y, int z, String worldName) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.worldName = worldName;
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
    
    public String getWorldName() {
        return worldName;
    }
    
    public double getStabilityStrength() {
        return Math.max(0.0D, Math.min(1.0D, stabilityStrength * (1.0D - damageFraction)));
    }
    
    public void setStabilityStrength(double strength) {
        this.stabilityStrength = strength;
    }
    
    public double getDamageFraction() {
        return Math.max(0.0D, Math.min(1.0D, damageFraction));
    }
    
    public void damage(double amount) {
        this.damageFraction += amount;
        if (damageFraction >= 1.0D) {
            this.isActive = false;
            this.damageFraction = 1.0D;
        }
    }
    
    public void heal(double amount) {
        this.damageFraction = Math.max(0.0D, damageFraction - amount);
        if (damageFraction < 1.0D) {
            this.isActive = true;
        }
    }
    
    public boolean isActive() {
        return isActive && damageFraction < 1.0D;
    }
    
    public void tick() {
        if (isActive) {
            age++;
        }
    }
    
    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("x", x);
        tag.setInteger("y", y);
        tag.setInteger("z", z);
        tag.setString("worldName", worldName);
        tag.setDouble("stabilityStrength", stabilityStrength);
        tag.setInteger("age", age);
        tag.setBoolean("isActive", isActive);
        tag.setDouble("damageFraction", damageFraction);
        return tag;
    }
    
    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        this.x = nbt.getInteger("x");
        this.y = nbt.getInteger("y");
        this.z = nbt.getInteger("z");
        this.worldName = nbt.getString("worldName");
        this.stabilityStrength = nbt.getDouble("stabilityStrength");
        this.age = nbt.getInteger("age");
        this.isActive = nbt.getBoolean("isActive");
        this.damageFraction = nbt.getDouble("damageFraction");
    }
}
