package net.vas.rdpcore.world;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.Constants;
import java.util.LinkedList;
import java.util.Deque;

/**
 * Tracks world mutation history - a log of significant reality distortion events.
 * Used for:
 * - Historical context and progression tracking
 * - Pressure calculation based on recent mutation density
 * - Detection of escalating distortion patterns
 * - Save/load persistence of world mutation timeline
 * 
 * Maintains a rolling history buffer (max 1000 events) to limit memory usage.
 */
public class WorldMutationHistory implements INBTSerializable<NBTTagCompound> {
    
    private static final int MAX_HISTORY_SIZE = 1000;
    
    /**
     * Represents a single mutation event in the world timeline
     */
    public static class MutationEvent implements INBTSerializable<NBTTagCompound> {
        public long timestamp;           // System time milliseconds
        public long worldTick;           // World tick when event occurred
        public String eventType;         // "ANOMALY", "MUTATION", "HOTSPOT", "SCAR", etc.
        public String cause;             // What triggered it
        public int regionX, regionZ;     // Region coordinates
        public double intensity;         // Severity/magnitude (0.0-1.0+)
        public String dimension;         // "minecraft:overworld", etc.
        public String notes;             // Additional context
        
        public MutationEvent() {
            // For deserialization
        }
        
        public MutationEvent(String eventType, String cause, int regionX, int regionZ, double intensity, String dimension) {
            this.timestamp = System.currentTimeMillis();
            this.worldTick = System.currentTimeMillis() / 50; // Approximate
            this.eventType = eventType;
            this.cause = cause;
            this.regionX = regionX;
            this.regionZ = regionZ;
            this.intensity = Math.max(0.0D, intensity);
            this.dimension = dimension;
            this.notes = "";
        }
        
        @Override
        public NBTTagCompound serializeNBT() {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setLong("timestamp", timestamp);
            tag.setLong("worldTick", worldTick);
            tag.setString("eventType", eventType);
            tag.setString("cause", cause);
            tag.setInteger("regionX", regionX);
            tag.setInteger("regionZ", regionZ);
            tag.setDouble("intensity", intensity);
            tag.setString("dimension", dimension);
            tag.setString("notes", notes);
            return tag;
        }
        
        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
            this.timestamp = nbt.getLong("timestamp");
            this.worldTick = nbt.getLong("worldTick");
            this.eventType = nbt.getString("eventType");
            this.cause = nbt.getString("cause");
            this.regionX = nbt.getInteger("regionX");
            this.regionZ = nbt.getInteger("regionZ");
            this.intensity = nbt.getDouble("intensity");
            this.dimension = nbt.getString("dimension");
            this.notes = nbt.getString("notes");
        }
    }
    
    private final Deque<MutationEvent> history = new LinkedList<>();
    private long totalEventsRecorded = 0;
    
    /**
     * Record a mutation event
     */
    public void recordEvent(String eventType, String cause, int regionX, int regionZ, double intensity, String dimension) {
        MutationEvent event = new MutationEvent(eventType, cause, regionX, regionZ, intensity, dimension);
        history.addLast(event);
        totalEventsRecorded++;
        
        // Maintain max size
        while (history.size() > MAX_HISTORY_SIZE) {
            history.removeFirst();
        }
    }
    
    /**
     * Get recent event count (last N events)
     */
    public int getRecentEventCount(int lastNEvents) {
        return Math.min(history.size(), lastNEvents);
    }
    
    /**
     * Calculate average intensity of recent events (for pressure calculation)
     * Used to detect escalating mutation patterns
     */
    public double getRecentAverageIntensity(int lastNEvents) {
        if (history.isEmpty()) return 0.0D;
        
        int count = 0;
        double totalIntensity = 0.0D;
        
        for (MutationEvent event : history) {
            if (count >= lastNEvents) break;
            totalIntensity += event.intensity;
            count++;
        }
        
        return count > 0 ? totalIntensity / count : 0.0D;
    }
    
    /**
     * Count events of a specific type in recent history
     */
    public int countEventType(String eventType, int lastNEvents) {
        int count = 0;
        int checked = 0;
        
        for (MutationEvent event : history) {
            if (checked >= lastNEvents) break;
            if (eventType.equals(event.eventType)) {
                count++;
            }
            checked++;
        }
        
        return count;
    }
    
    /**
     * Get the most recent event (or null if empty)
     */
    public MutationEvent getLastEvent() {
        return history.isEmpty() ? null : history.getLast();
    }
    
    /**
     * Get total number of events recorded (across all history, including trimmed events)
     */
    public long getTotalEventsRecorded() {
        return totalEventsRecorded;
    }
    
    /**
     * Get current history size
     */
    public int getHistorySize() {
        return history.size();
    }
    
    /**
     * Clear entire history (for testing or reset)
     */
    public void clearHistory() {
        history.clear();
    }
    
    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        
        // Serialize history events
        NBTTagList eventList = new NBTTagList();
        for (MutationEvent event : history) {
            eventList.appendTag(event.serializeNBT());
        }
        tag.setTag("history", eventList);
        tag.setLong("totalEvents", totalEventsRecorded);
        
        return tag;
    }
    
    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        history.clear();
        
        if (nbt.hasKey("history")) {
            NBTTagList eventList = nbt.getTagList("history", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < eventList.tagCount(); i++) {
                MutationEvent event = new MutationEvent();
                event.deserializeNBT(eventList.getCompoundTagAt(i));
                history.addLast(event);
            }
        }
        
        this.totalEventsRecorded = nbt.getLong("totalEvents");
    }
}
