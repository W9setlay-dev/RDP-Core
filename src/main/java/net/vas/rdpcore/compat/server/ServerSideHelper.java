package net.vas.rdpcore.compat.server;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Server-only helper class for operations that require direct MinecraftServer access.
 * This class should ONLY be referenced from server-side code.
 * 
 * Using @SideOnly(Side.SERVER) prevents this class from being loaded on the client,
 * eliminating risks of ClassNotFoundException for server-only classes.
 * 
 * Now uses RDPServerContext instead of reflection hacks.
 */
@SideOnly(Side.SERVER)
public final class ServerSideHelper {
    
    private static final Logger LOGGER = LogManager.getLogger("rdpcore.compat");
    
    private ServerSideHelper() {
        // Static utility class
    }
    
    /**
     * Check if the current server is an integrated (singleplayer) server.
     * Only called from server-side code.
     * 
     * @return true if running on integrated server, false otherwise
     */
    public static boolean isIntegratedServer() {
        try {
            MinecraftServer server = getCurrentServer();
            
            if (server == null) {
                return false;
            }
            
            // Safe to reference IntegratedServer here since we're @SideOnly(SERVER)
            return server instanceof net.minecraft.server.integrated.IntegratedServer;
        } catch (Throwable t) {
            LOGGER.debug("Error checking server type: {}", t.getMessage());
            return false;
        }
    }
    
    /**
     * Get the current MinecraftServer instance (server-side only).
     * Returns null if not available.
     * Uses RDPServerContext for safe access.
     */
    public static MinecraftServer getCurrentServer() {
        try {
            return net.vas.rdpcore.api.RDPAPI.getMinecraftServer();
        } catch (Throwable t) {
            LOGGER.debug("Error getting server: {}", t.getMessage());
            return null;
        }
    }
}

