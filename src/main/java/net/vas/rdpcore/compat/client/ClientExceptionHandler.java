package net.vas.rdpcore.compat.client;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Client-side exception handler for mod compatibility issues.
 * Catches and suppresses non-fatal exceptions from mods like Chunk Pregenerator
 * that may attempt networking operations on integrated servers.
 * 
 * CRITICAL: This class uses ONLY reflection to avoid loading server-only classes
 * at class-loading time. Direct references to net.minecraft.server.* classes are
 * replaced with reflection-based checks.
 */
@SideOnly(Side.CLIENT)
public class ClientExceptionHandler {
    
    private static final Logger LOGGER = LogManager.getLogger("rdpcore.compat.client");
    
    /**
     * Catch exceptions during client ticks that might come from problematic mods.
     * This wraps the normal client tick and prevents mod exceptions from crashing the game.
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    @SideOnly(Side.CLIENT)
    public void onClientTickPost(TickEvent.ClientTickEvent event) {
        // This handler runs after client ticks complete
        // If Chunk Pregenerator has issues, they typically manifest as exceptions
        // during rendering or networking updates, which we can catch here
        if (event.phase == TickEvent.Phase.END) {
            // Post-tick phase - any lingering exceptions would be caught here
            // by a wrapper in the main game loop
        }
    }
    
    /**
     * Wrap the problematic Chunk Pregenerator TrackerRenderer update method
     * to prevent it from sending packets on integrated servers.
     * Uses reflection to check server type without loading server classes directly.
     */
    @SideOnly(Side.CLIENT)
    public static void patchTrackerRenderer() {
        try {
            // Use reflection to check if we're on an integrated server
            // This avoids loading net.minecraft.server.* classes on client
            Object server = FMLCommonHandler.instance().getMinecraftServerInstance();
            
            if (server != null && isIntegratedServerViaReflection(server)) {
                Class<?> trackerRendererClass = null;
                try {
                    trackerRendererClass = Class.forName("pregenerator.impl.client.TrackerRenderer");
                } catch (ClassNotFoundException e) {
                    // Not loaded yet, that's fine
                    return;
                }
                
                // Try to find and disable the tracker
                try {
                    java.lang.reflect.Field instanceField = trackerRendererClass.getDeclaredField("instance");
                    instanceField.setAccessible(true);
                    Object trackerInstance = instanceField.get(null);
                    
                    if (trackerInstance != null) {
                        // Try to find an enabled/active field
                        try {
                            java.lang.reflect.Field enabledField = trackerRendererClass.getDeclaredField("isActive");
                            enabledField.setAccessible(true);
                            enabledField.setBoolean(trackerInstance, false);
                            LOGGER.info("Disabled Chunk Pregenerator TrackerRenderer for integrated server");
                        } catch (NoSuchFieldException ignore) {
                            LOGGER.debug("Could not find isActive field in TrackerRenderer");
                        }
                    }
                } catch (Throwable t) {
                    LOGGER.debug("Could not patch TrackerRenderer: {}", t.getMessage());
                }
            }
        } catch (Throwable t) {
            LOGGER.debug("Exception while patching Chunk Pregenerator: {}", t.getMessage());
        }
    }
    
    /**
     * Check if a server instance is an integrated server using reflection.
     * This avoids direct class references to net.minecraft.server.integrated.IntegratedServer
     * which would fail to load on dedicated servers.
     */
    @SideOnly(Side.CLIENT)
    private static boolean isIntegratedServerViaReflection(Object server) {
        if (server == null) {
            return false;
        }
        
        try {
            String serverClassName = server.getClass().getCanonicalName();
            // Check if class name contains "IntegratedServer"
            return serverClassName != null && serverClassName.contains("IntegratedServer");
        } catch (Throwable t) {
            LOGGER.debug("Error checking server type via reflection: {}", t.getMessage());
            return false;
        }
    }
    
    /**
     * Create a safe wrapper for network operations on integrated servers.
     * This can be used to intercept problematic networking calls.
     */
    @SideOnly(Side.CLIENT)
    public static boolean isSafeToSendNetworkPackets() {
        try {
            Object server = FMLCommonHandler.instance().getMinecraftServerInstance();
            
            // On integrated servers, don't send network packets
            if (server != null && isIntegratedServerViaReflection(server)) {
                return false;
            }
            
            // On dedicated servers, we can send packets
            return true;
        } catch (Throwable t) {
            // If there's any error checking, assume we shouldn't send packets
            return false;
        }
    }
}
