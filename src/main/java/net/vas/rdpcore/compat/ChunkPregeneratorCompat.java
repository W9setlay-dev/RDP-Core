package net.vas.rdpcore.compat;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.server.FMLServerHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.lang.reflect.Field;

/**
 * Compatibility handler for Chunk Pregenerator mod.
 * Prevents crashes when the mod tries to send packets on integrated servers.
 * 
 * The Chunk Pregenerator mod's TrackerRenderer attempts to send packets to the server
 * on every client tick, but in integrated (singleplayer) mode, there is no remote 
 * server connection, causing FMLCommonHandler.getClientToServerNetworkManager() to fail
 * with a RuntimeException: "Missing".
 * 
 * This handler disables the TrackerRenderer on integrated servers to prevent the crash.
 */
@SideOnly(Side.CLIENT)
public class ChunkPregeneratorCompat {
    
    private static final Logger LOGGER = LogManager.getLogger("rdpcore.compat");
    private static boolean isInitialized = false;
    
    /**
     * Initialize compatibility patches for Chunk Pregenerator.
     * Call this during mod initialization on the client side.
     */
    public static void init() {
        if (isInitialized) return;
        isInitialized = true;
        
        try {
            // Check if Chunk Pregenerator is loaded
            Class.forName("pregenerator.base.impl.networking.PacketHandler");
            LOGGER.info("Chunk Pregenerator detected - installing compatibility handler");
            
            // Attempt to disable the TrackerRenderer on integrated servers
            disableTrackerRendererOnIntegratedServer();
        } catch (ClassNotFoundException e) {
            // Chunk Pregenerator not loaded, nothing to patch
        } catch (Throwable t) {
            LOGGER.warn("Failed to install Chunk Pregenerator compatibility patch: {}", t.getMessage(), t);
        }
    }
    
    /**
     * Disable Chunk Pregenerator's TrackerRenderer when running on an integrated server.
     * Uses reflection to access and modify the renderer's internal state.
     * Also uses reflection to check server type to avoid loading server-only classes.
     */
    private static void disableTrackerRendererOnIntegratedServer() {
        try {
            // Check if we're running an integrated server using reflection
            // This avoids direct reference to net.minecraft.server.* classes
            Object server = FMLCommonHandler.instance().getMinecraftServerInstance();
            if (server != null && isIntegratedServerViaReflection(server)) {
                // Try to disable Chunk Pregenerator's client-side tracker
                disableTrackerInstance();
            }
        } catch (Throwable t) {
            LOGGER.debug("Could not pre-patch Chunk Pregenerator TrackerRenderer (may load later): {}", t.getMessage());
        }
    }
    
    /**
     * Check if a server instance is an integrated server using reflection.
     * Avoids direct class references to net.minecraft.server.integrated.IntegratedServer.
     */
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
     * Attempt to disable the Chunk Pregenerator TrackerRenderer instance.
     */
    private static void disableTrackerInstance() {
        try {
            Class<?> trackerRendererClass = Class.forName("pregenerator.impl.client.TrackerRenderer");
            
            // Try to find and disable the singleton instance
            try {
                // Try accessing the instance field
                Field instanceField = trackerRendererClass.getDeclaredField("instance");
                instanceField.setAccessible(true);
                Object trackerInstance = instanceField.get(null);
                
                if (trackerInstance != null) {
                    // Disable the tracker by setting isActive to false
                    try {
                        Field isActiveField = trackerRendererClass.getDeclaredField("isActive");
                        isActiveField.setAccessible(true);
                        isActiveField.setBoolean(trackerInstance, false);
                        LOGGER.info("Successfully disabled Chunk Pregenerator TrackerRenderer for integrated server");
                        return;
                    } catch (NoSuchFieldException ignore) {
                        // Try alternative field name
                    }
                    
                    // Try alternative field names
                    String[] fieldNames = {"enabled", "active", "tracking", "shouldRender"};
                    for (String fieldName : fieldNames) {
                        try {
                            Field field = trackerRendererClass.getDeclaredField(fieldName);
                            field.setAccessible(true);
                            field.setBoolean(trackerInstance, false);
                            LOGGER.info("Successfully disabled Chunk Pregenerator TrackerRenderer (field: {}) for integrated server", fieldName);
                            return;
                        } catch (NoSuchFieldException ignore) {
                            // Continue to next field name
                        }
                    }
                }
            } catch (NoSuchFieldException ignore) {
                // Instance field might not exist or have a different name
            }
            
        } catch (ClassNotFoundException ignore) {
            // TrackerRenderer class not found - not loaded yet or mod not present
        } catch (Throwable t) {
            LOGGER.debug("Could not disable TrackerRenderer: {}", t.getMessage());
        }
    }
}
