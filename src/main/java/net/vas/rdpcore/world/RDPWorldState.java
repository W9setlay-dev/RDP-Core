package net.vas.rdpcore.world;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraft.nbt.NBTTagCompound;
import net.vas.rdpcore.core.GlobalRDPLevel;
import net.vas.rdpcore.region.RDPRegion;
import net.vas.rdpcore.entity.RealityAnchor;
import net.vas.rdpcore.entity.RealityAnchorCapability;

/**
 * World-level R.D.P. state management.
 * Persistent across saves, tracks global RDP, regional states, mutation history, and scars.
 */
public class RDPWorldState implements INBTSerializable<NBTTagCompound> {
    
    private final World world;
    private GlobalRDPLevel globalRDPLevel = new GlobalRDPLevel();
    private Map<Long, RDPRegion> regions = new HashMap<>();
    private Map<String, RealityAnchor> realityAnchors = new HashMap<>();
    
    // Hotspots active in the world (keyed by id)
    private Map<String, net.vas.rdpcore.region.Hotspot> hotspots = new HashMap<>();

    public Map<String, net.vas.rdpcore.region.Hotspot> getHotspots() { return hotspots; }
    
    // Global scar tracking (all scars across all regions)
    private Map<String, Scar> scars = new HashMap<>();
    
    // World mutation history (timeline of events)
    private WorldMutationHistory mutationHistory = new WorldMutationHistory();
    
    // Judgement Day flag - once true, reality operates under RDP-X rules
    private boolean judgementDayActive = false;
    private long judgementDayStartTick = 0L;
    
    public RDPWorldState(World world) {
        this.world = world;
    }
    
    public World getWorld() {
        return world;
    }
    
    public GlobalRDPLevel getGlobalRDPLevel() {
        return globalRDPLevel;
    }

    public Map<String, RealityAnchor> getRealityAnchors() {
        return realityAnchors;
    }

    public void addRealityAnchor(String id, RealityAnchor anchor) {
        if (id != null && anchor != null) {
            realityAnchors.put(id, anchor);
            getOrCreateRegion(anchor.getX() >> 4, anchor.getZ() >> 4).setRealityAnchorCount(
                countActiveAnchorsInRegion(anchor.getX() >> 4, anchor.getZ() >> 4));
        }
    }

    public RealityAnchor registerPlacedAnchor(int x, int y, int z, String blockId, int metadata) {
        net.vas.rdpcore.entity.RealityAnchorDefinition definition =
            net.vas.rdpcore.entity.RealityAnchorRegistry.find(blockId, metadata);
        if (definition == null) return null;
        String id = "block:" + x + ":" + y + ":" + z;
        RealityAnchor anchor = new RealityAnchor(x, y, z, world.getWorldInfo().getWorldName(), definition);
        addRealityAnchor(id, anchor);
        return anchor;
    }

    public RealityAnchor removePlacedAnchor(int x, int y, int z) {
        return removeRealityAnchor("block:" + x + ":" + y + ":" + z);
    }

    public void tickRealityAnchors() {
        for (RealityAnchor anchor : realityAnchors.values()) anchor.tick();
    }

    public RealityAnchor removeRealityAnchor(String id) {
        RealityAnchor removed = realityAnchors.remove(id);
        if (removed != null) {
            getOrCreateRegion(removed.getX() >> 4, removed.getZ() >> 4).setRealityAnchorCount(
                countActiveAnchorsInRegion(removed.getX() >> 4, removed.getZ() >> 4));
        }
        return removed;
    }

    public double getAnchorSuppression(int x, int y, int z, RealityAnchorCapability capability) {
        double suppression = 0.0D;
        for (RealityAnchor anchor : realityAnchors.values()) {
            if (anchor.hasCapability(capability)) suppression += anchor.getInfluenceAt(x, y, z);
        }
        return Math.min(1.0D, Math.max(0.0D, suppression));
    }

    public double getAnchorInfluence(int x, int y, int z, RealityAnchorCapability capability) {
        return getAnchorSuppression(x, y, z, capability);
    }

    private int countActiveAnchorsInRegion(int chunkX, int chunkZ) {
        int count = 0;
        for (RealityAnchor anchor : realityAnchors.values()) {
            if (anchor.isActive() && (anchor.getX() >> 4) == chunkX && (anchor.getZ() >> 4) == chunkZ) count++;
        }
        return count;
    }
    
    /**
     * Get or create a region at the given chunk coordinates.
     * Regions are 16x16 chunks in size (256x256 blocks).
     */
    public RDPRegion getOrCreateRegion(int chunkX, int chunkZ) {
        int regionX = chunkX / 16;
        int regionZ = chunkZ / 16;
        long key = getRegionKey(regionX, regionZ);
        
        if (!regions.containsKey(key)) {
            regions.put(key, new RDPRegion(regionX, regionZ));
        }
        return regions.get(key);
    }
    
    public RDPRegion getRegion(int regionX, int regionZ) {
        long key = getRegionKey(regionX, regionZ);
        return regions.get(key);
    }
    
    public Map<Long, RDPRegion> getAllRegions() {
        return regions;
    }
    
    public boolean isJudgementDayActive() {
        return judgementDayActive;
    }
    
    public void setJudgementDayActive(boolean active) {
        this.judgementDayActive = active;
        if (active && judgementDayStartTick == 0) {
            this.judgementDayStartTick = System.currentTimeMillis();
        }
    }
    
    public long getJudgementDayStartTick() {
        return judgementDayStartTick;
    }
    
    public static long getRegionKey(int regionX, int regionZ) {
        return ((long) regionX << 32) | (regionZ & 0xFFFFFFFFL);
    }
    
    /**
     * Get all global scars (across all regions)
     */
    public Map<String, Scar> getScars() {
        return scars;
    }
    
    /**
     * Add a scar to global tracking and region-local tracking
     */
    public void addScar(String scarId, Scar scar) {
        scars.put(scarId, scar);
        // Also add to the region
        RDPRegion region = getOrCreateRegion(scar.getRegionX() * 16, scar.getRegionZ() * 16);
        region.addScar(scarId, scar);
    }
    
    /**
     * Get the world mutation history tracker
     */
    public WorldMutationHistory getMutationHistory() {
        return mutationHistory;
    }
    
    /**
     * Record a mutation event in the world history
     */
    public void recordMutationEvent(String eventType, String cause, int regionX, int regionZ, double intensity, String dimension) {
        mutationHistory.recordEvent(eventType, cause, regionX, regionZ, intensity, dimension);
    }
    
    /**
     * Calculate total scar pressure across the world
     * Used as a contribution to global RDP pressure
     */
    public double getTotalScarPressure() {
        double totalPressure = 0.0D;
        for (Scar scar : scars.values()) {
            totalPressure += scar.getPressureContribution();
        }
        // Normalize to 0.0-1.0 range
        return Math.min(1.0D, totalPressure / Math.max(1.0D, scars.size()));
    }
    
    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        
        // Serialize global RDP level
        tag.setTag("globalRDP", globalRDPLevel.serializeNBT());
        
        // Serialize Judgement Day state
        tag.setBoolean("judgementDayActive", judgementDayActive);
        tag.setLong("judgementDayStartTick", judgementDayStartTick);
        
        // Serialize regional keys and hotspots list
        NBTTagCompound regionsTag = new NBTTagCompound();
        for (Map.Entry<Long, RDPRegion> e : regions.entrySet()) {
            regionsTag.setTag(Long.toString(e.getKey()), e.getValue().serializeNBT());
        }
        tag.setTag("regions", regionsTag);

        NBTTagCompound anchorsTag = new NBTTagCompound();
        for (Map.Entry<String, RealityAnchor> anchor : realityAnchors.entrySet()) {
            anchorsTag.setTag(anchor.getKey(), anchor.getValue().serializeNBT());
        }
        tag.setTag("realityAnchors", anchorsTag);

        // Hotspots
        NBTTagCompound hotspotsTag = new NBTTagCompound();
        for (Map.Entry<String, net.vas.rdpcore.region.Hotspot> h : hotspots.entrySet()) {
            hotspotsTag.setTag(h.getKey(), h.getValue().serializeNBT());
        }

        tag.setTag("hotspots", hotspotsTag);
        
        // Serialize scars
        NBTTagCompound scarsTag = new NBTTagCompound();
        for (Map.Entry<String, Scar> e : scars.entrySet()) {
            scarsTag.setTag(e.getKey(), e.getValue().serializeNBT());
        }
        tag.setTag("scars", scarsTag);
        
        // Serialize mutation history
        tag.setTag("mutationHistory", mutationHistory.serializeNBT());

        return tag;
    }
    
    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        if (nbt.hasKey("globalRDP")) {
            globalRDPLevel.deserializeNBT(nbt.getCompoundTag("globalRDP"));
        }
        
        this.judgementDayActive = nbt.getBoolean("judgementDayActive");
        this.judgementDayStartTick = nbt.getLong("judgementDayStartTick");
        
        // Deserialize regions
        if (nbt.hasKey("regions")) {
            NBTTagCompound regionsTag = nbt.getCompoundTag("regions");
            this.regions.clear();
            for (String k : regionsTag.getKeySet()) {
                try {
                    long key = Long.parseLong(k);
                    RDPRegion r = new RDPRegion(0,0);
                    r.deserializeNBT(regionsTag.getCompoundTag(k));
                    this.regions.put(key, r);
                } catch (NumberFormatException ex) {
                    // ignore
                }
            }
        }

        if (nbt.hasKey("realityAnchors")) {
            NBTTagCompound anchorsTag = nbt.getCompoundTag("realityAnchors");
            this.realityAnchors.clear();
            for (String id : anchorsTag.getKeySet()) {
                RealityAnchor anchor = new RealityAnchor(0, 0, 0, "");
                anchor.deserializeNBT(anchorsTag.getCompoundTag(id));
                this.realityAnchors.put(id, anchor);
            }
        }

        // Deserialize hotspots
        if (nbt.hasKey("hotspots")) {
            NBTTagCompound hotspotsTag = nbt.getCompoundTag("hotspots");
            this.hotspots.clear();
            for (String id : hotspotsTag.getKeySet()) {
                net.vas.rdpcore.region.Hotspot h = new net.vas.rdpcore.region.Hotspot();
                h.deserializeNBT(hotspotsTag.getCompoundTag(id));
                this.hotspots.put(id, h);
            }
        }
        
        // Deserialize scars
        if (nbt.hasKey("scars")) {
            NBTTagCompound scarsTag = nbt.getCompoundTag("scars");
            this.scars.clear();
            for (String id : scarsTag.getKeySet()) {
                Scar scar = new Scar();
                scar.deserializeNBT(scarsTag.getCompoundTag(id));
                this.scars.put(id, scar);
            }
        }
        
        // Deserialize mutation history
        if (nbt.hasKey("mutationHistory")) {
            this.mutationHistory.deserializeNBT(nbt.getCompoundTag("mutationHistory"));
        }
    }
}
