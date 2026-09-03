package net.vas.rdpcore.region;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.Constants;
import net.vas.rdpcore.world.Scar;

/**
 * Represents a regional RDP state at chunk-region granularity (16x16 chunks = 256x256 blocks).
 * Each region tracks its own local distortion level, pressure, anomalies, and history.
 * 
 * Regions use a scalable storage model:
 * - Only loaded/modified regions are kept in memory
 * - Regions serialize to world NBT on save
 * - Regions lazy-load from NBT on demand
 */
public class RDPRegion implements INBTSerializable<NBTTagCompound> {
    
    private final int regionX;
    private final int regionZ;
    
    // Local distortion level (0.0 to 1.0, can exceed 1.0 for extreme cases)
    private double localRDPLevel = 0.0D;
    
    // Pressure represents how much RDP "force" is acting on this region
    // Pressure accumulates from: SRP evolution, parasite activity, SCP presence,
    // anomalous entities, structures, player interaction, dimensional activity, etc.
    private double pressure = 0.0D;
    
    // History of major events in this region
    private Map<String, Long> eventHistory = new HashMap<>();
    
    // Anomalies present in this region
    private Map<String, AnomalyData> anomalies = new HashMap<>();
    
    // Reality anchors can temporarily resist RDP progression
    private int realityAnchorCount = 0;
    
    // RDP "scars" - permanent marks left by reality instability
    private Map<String, Scar> scars = new HashMap<>();
    
    // Tick when this region was last updated
    private long lastUpdateTick = 0L;
    
    public RDPRegion(int regionX, int regionZ) {
        this.regionX = regionX;
        this.regionZ = regionZ;
    }
    
    public int getRegionX() {
        return regionX;
    }
    
    public int getRegionZ() {
        return regionZ;
    }
    
    public double getLocalRDPLevel() {
        return Math.max(0.0D, localRDPLevel);
    }
    
    public void setLocalRDPLevel(double level) {
        this.localRDPLevel = level;
        this.lastUpdateTick = System.currentTimeMillis();
    }
    
    public void addLocalRDP(double delta) {
        setLocalRDPLevel(getLocalRDPLevel() + delta);
    }
    
    public double getPressure() {
        return Math.max(0.0D, pressure);
    }
    
    public void setPressure(double p) {
        this.pressure = p;
    }
    
    public void addPressure(double delta) {
        setPressure(getPressure() + delta);
    }
    
    public long getLastEventTime(String eventType) {
        return eventHistory.getOrDefault(eventType, 0L);
    }
    
    public void recordEvent(String eventType, long timestamp) {
        eventHistory.put(eventType, timestamp);
    }
    
    public Map<String, AnomalyData> getAnomalies() {
        return anomalies;
    }
    
    public void addAnomaly(String anomalyId, AnomalyData data) {
        anomalies.put(anomalyId, data);
    }
    
    public void removeAnomaly(String anomalyId) {
        anomalies.remove(anomalyId);
    }
    
    public int getRealityAnchorCount() {
        return realityAnchorCount;
    }
    
    public void setRealityAnchorCount(int count) {
        this.realityAnchorCount = Math.max(0, count);
    }
    
    public Map<String, Scar> getScars() {
        return scars;
    }
    
    public int getScarCount() {
        return scars.size();
    }
    
    public void addScar(String scarId, Scar scar) {
        scars.put(scarId, scar);
    }
    
    public void removeScar(String scarId) {
        scars.remove(scarId);
    }
    
    /**
     * Calculate total pressure contribution from all scars in this region
     */
    public double getScarPressure() {
        double totalPressure = 0.0D;
        for (Scar scar : scars.values()) {
            totalPressure += scar.getPressureContribution();
        }
        return Math.min(1.0D, totalPressure);
    }
    
    /**
     * Escalate all scars based on current RDP level
     * Called during simulation to evolve scars over time
     */
    public void escalateScars(double globalRDPLevel) {
        for (Scar scar : scars.values()) {
            scar.escalate(globalRDPLevel);
        }
    }
    
    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("regionX", regionX);
        tag.setInteger("regionZ", regionZ);
        tag.setDouble("localRDPLevel", localRDPLevel);
        tag.setDouble("pressure", pressure);
        tag.setInteger("realityAnchorCount", realityAnchorCount);
        tag.setLong("lastUpdateTick", lastUpdateTick);
        
        // Serialize anomalies
        NBTTagList anomalyList = new NBTTagList();
        for (Map.Entry<String, AnomalyData> entry : anomalies.entrySet()) {
            NBTTagCompound anomalyTag = new NBTTagCompound();
            anomalyTag.setString("id", entry.getKey());
            anomalyTag.setTag("data", entry.getValue().serializeNBT());
            anomalyList.appendTag(anomalyTag);
        }
        tag.setTag("anomalies", anomalyList);
        
        // Serialize scars
        NBTTagList scarList = new NBTTagList();
        for (Map.Entry<String, Scar> entry : scars.entrySet()) {
            NBTTagCompound scarTag = new NBTTagCompound();
            scarTag.setString("id", entry.getKey());
            scarTag.setTag("scar", entry.getValue().serializeNBT());
            scarList.appendTag(scarTag);
        }
        tag.setTag("scars", scarList);
        
        return tag;
    }
    
    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        this.localRDPLevel = nbt.getDouble("localRDPLevel");
        this.pressure = nbt.getDouble("pressure");
        this.realityAnchorCount = nbt.getInteger("realityAnchorCount");
        this.lastUpdateTick = nbt.getLong("lastUpdateTick");
        
        // Deserialize anomalies
        NBTTagList anomalyList = nbt.getTagList("anomalies", Constants.NBT.TAG_COMPOUND);
        this.anomalies.clear();
        for (int i = 0; i < anomalyList.tagCount(); i++) {
            NBTTagCompound anomalyTag = anomalyList.getCompoundTagAt(i);
            String id = anomalyTag.getString("id");
            AnomalyData data = new AnomalyData();
            data.deserializeNBT(anomalyTag.getCompoundTag("data"));
            this.anomalies.put(id, data);
        }
        
        // Deserialize scars
        NBTTagList scarList = nbt.getTagList("scars", Constants.NBT.TAG_COMPOUND);
        this.scars.clear();
        for (int i = 0; i < scarList.tagCount(); i++) {
            NBTTagCompound scarTag = scarList.getCompoundTagAt(i);
            String id = scarTag.getString("id");
            Scar scar = new Scar();
            scar.deserializeNBT(scarTag.getCompoundTag("scar"));
            this.scars.put(id, scar);
        }
    }
    
    /**
     * Simple data holder for anomalies within a region
     */
    public static class AnomalyData implements INBTSerializable<NBTTagCompound> {
        public String type; // SPATIAL, TEMPORAL, DIMENSIONAL, etc.
        public double intensity;
        public int age; // ticks
        public int x, y, z; // position within region
        
        @Override
        public NBTTagCompound serializeNBT() {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("type", type);
            tag.setDouble("intensity", intensity);
            tag.setInteger("age", age);
            tag.setInteger("x", x);
            tag.setInteger("y", y);
            tag.setInteger("z", z);
            return tag;
        }
        
        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
            this.type = nbt.getString("type");
            this.intensity = nbt.getDouble("intensity");
            this.age = nbt.getInteger("age");
            this.x = nbt.getInteger("x");
            this.y = nbt.getInteger("y");
            this.z = nbt.getInteger("z");
        }
    }
}
