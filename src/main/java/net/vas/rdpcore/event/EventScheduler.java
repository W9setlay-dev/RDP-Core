package net.vas.rdpcore.event;

import net.minecraft.world.WorldServer;
import net.vas.rdpcore.world.RDPWorldState;
import net.vas.rdpcore.region.RDPRegion;
import net.vas.rdpcore.config.RDPConfig;
import net.vas.rdpcore.core.GlobalRDPLevel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

/**
 * Lightweight event scheduler for RDP events. Selects weighted events per world/region.
 */
public class EventScheduler {

    private static final Logger LOGGER = LogManager.getLogger("rdpcore");

    public static void tickWorld(RDPWorldState state) {
        if (state == null) return;
        WorldServer world = (WorldServer) state.getWorld();
        long tick = world.getTotalWorldTime();
        Random r = new Random(state.getWorld().getSeed() ^ tick);

        // Event weighting by global stage
        double global = state.getGlobalRDPLevel().getLevel();
        GlobalRDPLevel.RDPStage stage = state.getGlobalRDPLevel().getCurrentStage();
        RDPConfig.StageModifiers mods = RDPConfig.getStageModifiers(stage);

        double microChance = Math.min(0.05D, (0.001D + global * 0.02D) * mods.eventMultiplier);
        double minorChance = Math.min(0.01D, (0.0005D + global * 0.008D) * mods.eventMultiplier);
        double majorChance = Math.min(0.002D, (0.0001D + global * 0.002D) * mods.eventMultiplier);

        // Micro events
        if (r.nextDouble() < microChance) {
            for (RDPRegion reg : state.getAllRegions().values()) {
                if (r.nextDouble() < 0.005D) {
                    reg.addPressure(0.01D);
                    RDPEvents.post(new RDPEvents.RegionalRDPChangeEvent(reg.getRegionX()*16, reg.getRegionZ()*16, reg.getLocalRDPLevel(), reg.getLocalRDPLevel()+0.01));
                }
            }
        }

        // Minor events
        if (r.nextDouble() < minorChance) {
            for (RDPRegion reg : state.getAllRegions().values()) {
                if (r.nextDouble() < 0.002D) {
                    // spawn small hotspot or small mutation
                    net.vas.rdpcore.region.HotspotManager.spawnHotspot(state, reg.getRegionX()*16 + 8, 64, reg.getRegionZ()*16 + 8, 8, 0.2D, "minor");
                    reg.addPressure(0.02D);
                    LOGGER.debug("Minor event: hotspot spawned in region {}", reg.getRegionX()+","+reg.getRegionZ());
                    break;
                }
            }
        }

        // Major events
        if (r.nextDouble() < majorChance) {
            for (RDPRegion reg : state.getAllRegions().values()) {
                if (r.nextDouble() < 0.001D) {
                    // schedule a major mutation
                    int cx = reg.getRegionX()*16 + 8;
                    int cz = reg.getRegionZ()*16 + 8;
                    net.vas.rdpcore.mutation.MutationRequest req = net.vas.rdpcore.mutation.MutationRequest.builder()
                        .center(cx, cz)
                        .radius(4)
                        .profile("rdp_major_event")
                        .intensity(0.8f)
                        .priority(80)
                        .budget((int)(net.vas.rdpcore.config.RDPConfig.MUTATION_NORMAL_BUDGET * 5 * mods.mutationMultiplier))
                        .cause("RDP_MAJOR_EVENT")
                        .dimension(state.getWorld().provider.getDimensionType().getName())
                        .build();
                    net.vas.rdpcore.mutation.MutationCoordinator.getInstance().queueMutation(req, 100);
                    LOGGER.warn("Major event scheduled at region {}", reg.getRegionX()+","+reg.getRegionZ());
                    break;
                }
            }
        }
    }
}
