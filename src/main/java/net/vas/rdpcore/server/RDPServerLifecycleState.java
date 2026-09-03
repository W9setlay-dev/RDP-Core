package net.vas.rdpcore.server;

/**
 * Lifecycle state machine for RDP server context.
 * Ensures clear ownership and safe transitions.
 */
public enum RDPServerLifecycleState {
    /**
     * Initial state - server object not yet available
     */
    NEW,
    
    /**
     * Server object received but worlds not yet initialized
     */
    SERVER_INITIALIZED,
    
    /**
     * Server is running and worlds are active
     */
    RUNNING,
    
    /**
     * Server is stopping, worlds being unloaded
     */
    STOPPING,
    
    /**
     * Server and all worlds completely stopped
     */
    STOPPED
}
