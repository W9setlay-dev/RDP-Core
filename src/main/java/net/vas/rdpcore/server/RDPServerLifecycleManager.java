package net.vas.rdpcore.server;

import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility bridge for server lifecycle state transitions.
 *
 * IMPORTANT: FMLServerStartingEvent and FMLServerStoppingEvent are mod lifecycle events,
 * not Forge world/event-bus events. They must be handled by @Mod.EventHandler methods on
 * the mod class, not by registering this class on MinecraftForge.EVENT_BUS.
 */
public class RDPServerLifecycleManager {
    
    private static final Logger LOGGER = LogManager.getLogger("rdpcore");

    public static void initializeServer(MinecraftServer server) {
        try {
            RDPServerContext context = RDPServerContext.getInstance();
            context.initializeWithServer(server);
            context.markRunning();
            LOGGER.info("[RDP] Server lifecycle manager initialization complete");
        } catch (Throwable t) {
            LOGGER.error("[RDP] Failed to initialize server context: {}", t.getMessage(), t);
        }
    }

    public static void shutdownServer() {
        try {
            RDPServerContext context = RDPServerContext.getInstance();
            context.initiateShutdown();
            LOGGER.info("[RDP] Server shutdown initiated");
        } catch (Throwable t) {
            LOGGER.error("[RDP] Failed during server shutdown: {}", t.getMessage(), t);
        }
    }
    
    /**
     * Mark shutdown as complete (called after all worlds unloaded)
     */
    public static void completeShutdown() {
        RDPServerContext.getInstance().completeShutdown();
    }
}
