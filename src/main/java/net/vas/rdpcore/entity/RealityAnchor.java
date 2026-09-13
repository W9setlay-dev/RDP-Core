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
    private double effectRadius = 64.0D;
    private java.util.EnumSet<RealityAnchorCapability> capabilities =
        java.util.EnumSet.of(RealityAnchorCapability.REALITY_STABILIZATION);
    
    public RealityAnchor(int x, int y, int z, String worldName) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.worldName = worldName;
    }

    public RealityAnchor(int x, int y, int z, String worldName, RealityAnchorDefinition definition) {
        this(x, y, z, worldName);
        if (definition != null) {
            this.effectRadius = definition.getRadius();
            this.stabilityStrength = definition.getStrength();
            this.capabilities = java.util.EnumSet.copyOf(definition.getCapabilities());
        }
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

    public int getAge() { return age; }
    public int getMaxAge() { return maxAge; }
    public void setMaxAge(int maxAge) {
        this.maxAge = Math.max(0, maxAge);
        if (age >= this.maxAge) isActive = false;
    }

    public double getEffectRadius() { return effectRadius; }
    public boolean hasCapability(RealityAnchorCapability capability) {
        return capability == null || capabilities.contains(capability);
    }

    /** Linear falloff, with full strength at the anchor and zero at its radius. */
    public double getInfluenceAt(int px, int py, int pz) {
        if (!isActive() || effectRadius <= 0.0D) return 0.0D;
        double distance = Math.sqrt((double)(x - px) * (x - px)
            + (double)(y - py) * (y - py) + (double)(z - pz) * (z - pz));
        if (distance >= effectRadius) return 0.0D;
        return getStabilityStrength() * (1.0D - distance / effectRadius);
    }
    
    public void tick() {
        if (isActive) {
            age++;
            if (age >= maxAge) isActive = false;
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
        tag.setInteger("maxAge", maxAge);
        tag.setBoolean("isActive", isActive);
        tag.setDouble("damageFraction", damageFraction);
        tag.setDouble("effectRadius", effectRadius);
        tag.setString("capabilities", capabilities.toString());
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
        this.maxAge = nbt.hasKey("maxAge") ? Math.max(0, nbt.getInteger("maxAge")) : Integer.MAX_VALUE;
        this.isActive = nbt.getBoolean("isActive");
        this.damageFraction = nbt.getDouble("damageFraction");
        this.effectRadius = nbt.hasKey("effectRadius") ? Math.max(0.0D, nbt.getDouble("effectRadius")) : 64.0D;
        this.capabilities = java.util.EnumSet.of(RealityAnchorCapability.REALITY_STABILIZATION);
        if (nbt.hasKey("capabilities")) {
            String value = nbt.getString("capabilities").replace("[", "").replace("]", "");
            for (String name : value.split(",")) {
                try { if (!name.trim().isEmpty()) capabilities.add(RealityAnchorCapability.valueOf(name.trim())); }
                catch (IllegalArgumentException ignored) { }
            }
        }
    }
}
