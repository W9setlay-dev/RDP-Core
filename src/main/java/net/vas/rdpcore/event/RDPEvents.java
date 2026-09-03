package net.vas.rdpcore.event;

import net.minecraftforge.common.MinecraftForge;

import net.minecraftforge.fml.common.eventhandler.Event;
import net.vas.rdpcore.core.GlobalRDPLevel;
import net.minecraft.world.World;

/**
 * RDP Events and helper methods.
 */
public class RDPEvents {

    public static abstract class RDPEventBase extends Event {
        protected long timestamp = System.currentTimeMillis();
        public long getTimestamp() { return timestamp; }
    }

    public static class RDPStageChangeEvent extends RDPEventBase {
        public final GlobalRDPLevel.RDPStage oldStage;
        public final GlobalRDPLevel.RDPStage newStage;
        public final double globalRDPLevel;
        public RDPStageChangeEvent(GlobalRDPLevel.RDPStage old, GlobalRDPLevel.RDPStage n, double level) {
            this.oldStage = old; this.newStage = n; this.globalRDPLevel = level;
        }
    }

    public static class AnomalySpawnEvent extends RDPEventBase {
        public final String anomalyType; public final int x, y, z; public final double intensity;
        public AnomalySpawnEvent(String type, int x, int y, int z, double intensity) { this.anomalyType = type; this.x = x; this.y = y; this.z = z; this.intensity = intensity; }
    }

    public static class JudgementDayEvent extends RDPEventBase {
        public final double finalRDPLevel; public JudgementDayEvent(double r) { this.finalRDPLevel = r; }
    }

    public static class RealityAnchorDestroyedEvent extends RDPEventBase {
        public final int x, y, z; public final String cause; public RealityAnchorDestroyedEvent(int x,int y,int z,String cause){this.x=x;this.y=y;this.z=z;this.cause=cause;}
    }

    public static class RegionalRDPChangeEvent extends RDPEventBase {
        public final int chunkX, chunkZ; public final double oldLevel, newLevel; public RegionalRDPChangeEvent(int cx,int cz,double o,double n){this.chunkX=cx;this.chunkZ=cz;this.oldLevel=o;this.newLevel=n;}
    }

    public static void post(Event e) { MinecraftForge.EVENT_BUS.post(e); }

    private static final java.util.Map<String, GlobalRDPLevel.RDPStage> lastStageByWorld = new java.util.concurrent.ConcurrentHashMap<>();

    public static void fireStageChangedIfNeeded(World world, GlobalRDPLevel level) {
        if (world == null || level == null) return;
        String key = world.getWorldInfo().getWorldName();
        GlobalRDPLevel.RDPStage prev = lastStageByWorld.getOrDefault(key, level.getCurrentStage());
        GlobalRDPLevel.RDPStage current = level.getCurrentStage();
        if (prev != current) {
            lastStageByWorld.put(key, current);
            post(new RDPStageChangeEvent(prev, current, level.getLevel()));
        }
    }
}
