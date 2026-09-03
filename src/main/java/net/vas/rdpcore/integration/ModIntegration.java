package net.vas.rdpcore.integration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Base class for mod integrations.
 * Each integration checks if the target mod is loaded before performing operations.
 */
public abstract class ModIntegration {
    
    protected static final Logger LOGGER = LogManager.getLogger("rdpcore");
    
    protected String modId;
    protected boolean isLoaded = false;
    
    public ModIntegration(String modId) {
        this.modId = modId;
    }
    
    /**
     * Check if the target mod is loaded
     */
    public abstract boolean checkModLoaded();
    
    /**
     * Initialize the integration
     */
    public abstract void init();
    
    /**
     * Get the mod ID
     */
    public String getModId() {
        return modId;
    }
    
    /**
     * Check if this integration is active
     */
    public boolean isActive() {
        return isLoaded;
    }
    
    protected void log(String message) {
        LOGGER.info("[" + modId + "] " + message);
    }
    
    protected void logError(String message, Throwable e) {
        LOGGER.error("[" + modId + "] " + message, e);
    }
}
