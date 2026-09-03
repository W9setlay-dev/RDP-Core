package net.vas.rdpcore.server;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Central authority for server state and world lifecycle in RDP Core.
 * 
 * Owns:
 * - MinecraftServer reference
 * - Lifecycle state
 * - World registrations
 * - Shutdown coordination
 * 
 * This replaces the fragile static initialization approach with explicit ownership.
 */
public class RDPServerContext {
    
    private static final Logger LOGGER = LogManager.getLogger("rdpcore");
    
    private static RDPServerContext INSTANCE;
    
    private MinecraftServer server;
    private RDPServerLifecycleState state;
    private final Map<Integer, WorldServer> registeredWorlds;
    private boolean shuttingDown;
    
    /**
     * Private constructor - use getInstance()
     */
    private RDPServerContext() {
        this.server = null;
        this.state = RDPServerLifecycleState.NEW;
        this.registeredWorlds = Collections.synchronizedMap(new HashMap<>());
        this.shuttingDown = false;
    }
    
    /**
     * Get singleton instance (thread-safe, create-on-demand)
     */
    public static synchronized RDPServerContext getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new RDPServerContext();
        }
        return INSTANCE;
    }
    
    /**
     * Initialize server context with the MinecraftServer instance.
     * Called once from RDPServerLifecycleManager.onServerStarting()
     * 
     * @param server The MinecraftServer instance from Forge event
     * @throws IllegalStateException if already initialized
     */
    public synchronized void initializeWithServer(MinecraftServer server) {
        Objects.requireNonNull(server, "MinecraftServer cannot be null");
        
        if (this.server != null) {
            LOGGER.warn("RDP Server Context already initialized with server. Ignoring duplicate initialization.");
            return;
        }
        
        this.server = server;
        this.state = RDPServerLifecycleState.SERVER_INITIALIZED;
        this.registeredWorlds.clear();
        this.shuttingDown = false;
        
        LOGGER.info("[RDP] Server context initialized with MinecraftServer: {}", server.getServerModName());
    }
    
    /**
     * Mark server as running (all worlds initialized)
     */
    public synchronized void markRunning() {
        if (state != RDPServerLifecycleState.SERVER_INITIALIZED) {
            LOGGER.warn("[RDP] Cannot mark running from state: {}", state);
            return;
        }
        this.state = RDPServerLifecycleState.RUNNING;
        LOGGER.info("[RDP] Server context marked RUNNING");
    }
    
    /**
     * Register a world with this server context
     */
    public synchronized void registerWorld(WorldServer world) {
        Objects.requireNonNull(world, "WorldServer cannot be null");
        int dimensionId = world.provider.getDimension();
        
        if (registeredWorlds.containsKey(dimensionId)) {
            LOGGER.debug("[RDP] World {} already registered", dimensionId);
            return;
        }
        
        registeredWorlds.put(dimensionId, world);
        LOGGER.info("[RDP] Registered world: dimension {}", dimensionId);
    }
    
    /**
     * Unregister a world (called on world unload)
     */
    public synchronized void unregisterWorld(int dimensionId) {
        registeredWorlds.remove(dimensionId);
        LOGGER.info("[RDP] Unregistered world: dimension {}", dimensionId);
    }
    
    /**
     * Initiate shutdown sequence
     */
    public synchronized void initiateShutdown() {
        if (shuttingDown) {
            LOGGER.debug("[RDP] Shutdown already initiated");
            return;
        }
        
        this.shuttingDown = true;
        this.state = RDPServerLifecycleState.STOPPING;
        LOGGER.info("[RDP] Server context shutdown initiated");
    }
    
    /**
     * Complete shutdown and clean all references
     */
    public synchronized void completeShutdown() {
        registeredWorlds.clear();
        server = null;
        state = RDPServerLifecycleState.STOPPED;
        shuttingDown = false;
        
        LOGGER.info("[RDP] Server context shutdown complete");
    }
    
    // ========== Query methods ==========
    
    /**
     * Get the MinecraftServer instance
     */
    public MinecraftServer getServer() {
        return server;
    }
    
    /**
     * Get current lifecycle state
     */
    public RDPServerLifecycleState getState() {
        return state;
    }
    
    /**
     * Check if server is initialized
     */
    public boolean isInitialized() {
        return server != null;
    }
    
    /**
     * Check if server is running
     */
    public boolean isRunning() {
        return state == RDPServerLifecycleState.RUNNING;
    }
    
    /**
     * Check if shutdown is in progress
     */
    public boolean isShuttingDown() {
        return shuttingDown;
    }
    
    /**
     * Get a registered world by dimension ID
     */
    public WorldServer getWorld(int dimensionId) {
        return registeredWorlds.get(dimensionId);
    }
    
    /**
     * Get all registered worlds (read-only copy)
     */
    public Map<Integer, WorldServer> getWorlds() {
        return new HashMap<>(registeredWorlds);
    }
    
    /**
     * Reset instance for testing or clean restart
     */
    static void resetInstance() {
        INSTANCE = null;
    }
}
