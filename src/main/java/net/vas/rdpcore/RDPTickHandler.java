package net.vas.rdpcore;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.vas.rdpcore.anomaly.interdimensional.InterdimensionalGravityManager;

/**
 * Server tick handler - drives RDP simulation on each server tick.
 * Gets server from RDPServerContext instead of FMLCommonHandler.
 */
public class RDPTickHandler {
    
    private static final Logger LOGGER = LogManager.getLogger("rdpcore");
    private static final RDPTickHandler INSTANCE = new RDPTickHandler();

    private RDPTickHandler() {}

    public static RDPTickHandler getInstance() {
        return INSTANCE;
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        
        // Get server from RDP context (no longer from FMLCommonHandler)
        MinecraftServer server = net.vas.rdpcore.api.RDPAPI.getMinecraftServer();
        if (server == null) return;

        try {
            for (WorldServer world : server.worlds) {
                if (world == null) continue;
            net.vas.rdpcore.world.RDPWorldState state =
                net.vas.rdpcore.api.RDPAPI.getWorldState(world);
            if (state != null && net.vas.rdpcore.config.RDPConfig.ENABLE_REALITY_ANCHORS) {
                state.tickRealityAnchors();
            }
            long totalTicks = world.getTotalWorldTime();
            if (state != null) {
                try {
                    InterdimensionalGravityManager.tick(world, state, totalTicks);
                } catch (Throwable t) {
                    LOGGER.error("[RDP] Gravity field failed for world {}: {}",
                        world.provider.getDimension(), t.getMessage(), t);
                }
            }
                if (totalTicks % net.vas.rdpcore.config.RDPConfig.SIMULATION_INTERVAL_TICKS == 0) {
                    try {
                        RDPSimulationEngine.runSimulationForWorld(world);
                    } catch (Throwable t) {
                        LOGGER.error("[RDP] Simulation failed for world {}: {}", 
                            world.provider.getDimensionType().getName(), t.getMessage(), t);
                    }
                }
            }
        } catch (Throwable t) {
            LOGGER.error("[RDP] Server tick handler error: {}", t.getMessage(), t);
        }
    }
}
