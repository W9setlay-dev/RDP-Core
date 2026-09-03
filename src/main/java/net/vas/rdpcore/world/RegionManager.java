package net.vas.rdpcore.world;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import net.vas.rdpcore.config.RDPConfig;
import net.vas.rdpcore.world.RDPWorldState;

/**
 * Maintains an LRU cache of active regions for simulation prioritization.
 */
public class RegionManager {

    private static final Logger LOGGER = LogManager.getLogger("rdpcore");
    private static final Map<WorldServer, RegionManager> INSTANCES = new HashMap<>();

    public static RegionManager get(WorldServer world) {
        return INSTANCES.computeIfAbsent(world, w -> new RegionManager(w));
    }

    private final WorldServer world;
    private final LinkedHashMap<Long, Long> lru = new LinkedHashMap<Long, Long>(RDPConfig.ACTIVE_REGION_CACHE_SIZE, 0.75f, true) {
        protected boolean removeEldestEntry(Map.Entry<Long, Long> eldest) {
            return size() > RDPConfig.ACTIVE_REGION_CACHE_SIZE;
        }
    };

    private RegionManager(WorldServer world) {
        this.world = world;
    }

    public void touchRegion(long regionKey) {
        synchronized (lru) {
            lru.put(regionKey, System.currentTimeMillis());
        }
    }

    public List<Long> getActiveRegionKeys() {
        synchronized (lru) {
            List<Long> keys = new ArrayList<>(lru.keySet());
            Collections.reverse(keys); // most-recent first
            return keys;
        }
    }

    public void scanForPlayerRegions() {
        // Add regions near players to active cache
        net.minecraftforge.fml.common.FMLCommonHandler fh = net.minecraftforge.fml.common.FMLCommonHandler.instance();
        MinecraftServer server = fh.getMinecraftServerInstance();
        if (server == null) return;
        for (Object obj : server.getPlayerList().getPlayers()) {
            if (!(obj instanceof EntityPlayerMP)) continue;
            EntityPlayerMP p = (EntityPlayerMP) obj;
            if (p.world != world) continue;
            int chunkX = p.chunkCoordX;
            int chunkZ = p.chunkCoordZ;
            int regionX = chunkX / RDPConfig.REGION_SIZE_CHUNKS;
            int regionZ = chunkZ / RDPConfig.REGION_SIZE_CHUNKS;
            long key = RDPWorldState.getRegionKey(regionX, regionZ);
            touchRegion(key);
        }
    }

}
