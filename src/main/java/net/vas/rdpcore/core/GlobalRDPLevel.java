package net.vas.rdpcore.core;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Global RDP Level - represents the progression of Reality Distortion Phenomenon
 * across the entire world. Values range from 0.0 (pristine) to 1.0 (RDP-X/Judgement Day).
 * 
 * Conceptual ranges:
 * 0.00 - 0.09  -> RDP-0  (Normal Minecraft, small anomalies)
 * 0.10 - 0.w24  -> RDP-I  (First distortions)
 * 0.25 - 0.39  -> RDP-II (Regional instability, biome anomalies)
 * 0.40 - 0.54  -> RDP-III (Persistent mutations, hotspots)
 * 0.55 - 0.69  -> RDP-IV (Reality instability, temporal effects)
 * 0.70 - 0.79  -> RDP-V (Dimensional leakage)
 * 0.80 - 0.89  -> RDP-VI (Spatial collapse, sky anomalies)
 * 0.90 - 0.96  -> RDP-VII (Cosmological instability)
 * 0.97 - 1.00  -> RDP-X (Reality breakdown)
 */
public class GlobalRDPLevel implements INBTSerializable<NBTTagCompound> {
    
    private double level = 0.0D;
    private long lastUpdateTick = 0L;
    private Map<String, Double> stageFractions = new HashMap<>();
    
    // Configuration for stage thresholds (can be customized)
    public static double STAGE_RDP0_MIN = 0.00D;
    public static double STAGE_RDPI_MIN = 0.10D;
    public static double STAGE_RDPII_MIN = 0.25D;
    public static double STAGE_RDPIII_MIN = 0.40D;
    public static double STAGE_RDPIV_MIN = 0.55D;
    public static double STAGE_RDPV_MIN = 0.70D;
    public static double STAGE_RDPVI_MIN = 0.80D;
    public static double STAGE_RDPVII_MIN = 0.90D;
    public static double STAGE_RDPX_MIN = 0.97D;
    
    public enum RDPStage {
        RDP0("RDP-0", 0.00D, 0.09D),
        RDPI("RDP-I", 0.10D, 0.24D),
        RDPII("RDP-II", 0.25D, 0.39D),
        RDPIII("RDP-III", 0.40D, 0.54D),
        RDPIV("RDP-IV", 0.55D, 0.69D),
        RDPV("RDP-V", 0.70D, 0.79D),
        RDPVI("RDP-VI", 0.80D, 0.89D),
        RDPVII("RDP-VII", 0.90D, 0.96D),
        RDPX("RDP-X", 0.97D, 1.00D);
        
        public final String displayName;
        public final double minLevel;
        public final double maxLevel;
        
        RDPStage(String displayName, double minLevel, double maxLevel) {
            this.displayName = displayName;
            this.minLevel = minLevel;
            this.maxLevel = maxLevel;
        }
    }
    
    /**
     * Get the current global RDP level (0.0 to 1.0)
     */
    public double getLevel() {
        return Math.max(0.0D, Math.min(1.0D, level));
    }
    
    /**
     * Set the global RDP level, clamping to 0.0-1.0 range
     */
    public void setLevel(double newLevel) {
        double clamped = Math.max(0.0D, Math.min(1.0D, newLevel));
        if (Math.abs(clamped - level) > 1e-6) {
            this.level = clamped;
            this.lastUpdateTick = System.currentTimeMillis();
        }
    }
    
    /**
     * Increment the global RDP level by a small amount
     */
    public void addLevel(double delta) {
        setLevel(getLevel() + delta);
    }
    
    /**
     * Get the current RDP stage based on level
     */
    public RDPStage getCurrentStage() {
        double lvl = getLevel();
        for (RDPStage stage : RDPStage.values()) {
            if (lvl >= stage.minLevel && lvl <= stage.maxLevel) {
                return stage;
            }
        }
        return RDPStage.RDP0;
    }
    
    /**
     * Check if we have crossed a stage threshold (used for triggering events)
     */
    public boolean hasReachedStage(RDPStage stage) {
        return getLevel() >= stage.minLevel;
    }
    
    public long getLastUpdateTick() {
        return lastUpdateTick;
    }
    
    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setDouble("level", level);
        tag.setLong("lastUpdateTick", lastUpdateTick);
        return tag;
    }
    
    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        this.level = nbt.getDouble("level");
        this.lastUpdateTick = nbt.getLong("lastUpdateTick");
    }
}
