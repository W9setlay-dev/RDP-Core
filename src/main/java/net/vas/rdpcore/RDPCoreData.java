package net.vas.rdpcore;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.vas.rdpcore.config.RDPConfig;

/**
 * Core data structures initialization for R.D.P.
 * Sets up global state holders and validates configuration.
 */
public class RDPCoreData {
    
    private static final Logger LOGGER = LogManager.getLogger("rdpcore");
    
    public static void init() {
        LOGGER.info("Initializing R.D.P. Core data structures...");
        
        // Validate configuration
        validateConfiguration();
        
        // Data structures are lazy-initialized on world load via RDPAPI
        // No pre-loading needed; regions and world states are created on-demand
        
        LOGGER.info("R.D.P. Core data structures ready.");
        LOGGER.info("Global RDP will start at: {}", RDPConfig.ENABLE_RDP_PROGRESSION ? "enabled" : "disabled");
        LOGGER.info("Simulation runs every {} ticks", RDPConfig.SIMULATION_INTERVAL_TICKS);
        LOGGER.info("Region size: {} chunks ({}x{} blocks)", 
            RDPConfig.REGION_SIZE_CHUNKS,
            RDPConfig.REGION_SIZE_CHUNKS * 16,
            RDPConfig.REGION_SIZE_CHUNKS * 16);
    }
    
    private static void validateConfiguration() {
        if (RDPConfig.REGION_SIZE_CHUNKS <= 0) {
            LOGGER.warn("Invalid REGION_SIZE_CHUNKS: {}, resetting to 16", RDPConfig.REGION_SIZE_CHUNKS);
            RDPConfig.REGION_SIZE_CHUNKS = 16;
        }
        
        if (RDPConfig.SIMULATION_INTERVAL_TICKS < 10) {
            LOGGER.warn("SIMULATION_INTERVAL_TICKS too low: {}, resetting to 200", RDPConfig.SIMULATION_INTERVAL_TICKS);
            RDPConfig.SIMULATION_INTERVAL_TICKS = 200;
        }
        
        if (RDPConfig.CHUNK_REWRITE_BUDGET_PER_TICK < 1) {
            LOGGER.warn("CHUNK_REWRITE_BUDGET_PER_TICK must be >= 1, resetting to 5");
            RDPConfig.CHUNK_REWRITE_BUDGET_PER_TICK = 5;
        }
        
        LOGGER.debug("Configuration validated successfully.");
    }
}
