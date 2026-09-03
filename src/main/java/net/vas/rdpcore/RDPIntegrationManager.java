package net.vas.rdpcore;

import net.vas.rdpcore.integration.ModIntegration;
import net.vas.rdpcore.integration.scp.SCP001Integration;
import net.vas.rdpcore.integration.modpack.SRPIntegration;
import net.vas.rdpcore.integration.modpack.GameStagesIntegration;
import net.vas.rdpcore.util.PressureRegistry;
import net.vas.rdpcore.util.SRPPressureSource;
import net.vas.rdpcore.util.SCPPressureSource;
import net.vas.rdpcore.util.PlayerPressureSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Manages integrations with other mods and systems.
 * Initializes compatibility layers for:
 * - RDP Chunk Rewriter (mutation submission via ChunkRewriterBridge)
 * - SCP-001 Controller / SCP Project Anomalous (scenario progression)
 * - Scape and Run: Parasites (SRP) (evolution scaling)
 * - GameStages (progression gating)
 * 
 * Also registers pressure sources:
 * - Player pressure (entity count)
 * - SRP pressure (parasite entities)
 * - SCP pressure (SCP entities)
 */
public class RDPIntegrationManager {
    
    private static final Logger LOGGER = LogManager.getLogger("rdpcore");
    
    public static void initializeIntegrations() {
        LOGGER.info("=".repeat(80));
        LOGGER.info("Initializing modpack integrations...");
        LOGGER.info("=".repeat(80));
        
        // Register pressure sources
        initializePressureSources();
        
        // Initialize mod integrations
        initializeModIntegrations();
        
        LOGGER.info("=".repeat(80));
        LOGGER.info("Modpack integrations complete.");
        LOGGER.info("=".repeat(80));
    }
    
    /**
     * Register all pressure sources with the pressure registry
     */
    private static void initializePressureSources() {
        LOGGER.info("Registering pressure sources...");
        
        // Player pressure source (baseline - player count and activity)
        PressureRegistry.register(new PlayerPressureSource(1.0D));
        LOGGER.info("  [OK] Player pressure source registered");
        
        // SRP pressure source (parasite count and evolution)
        PressureRegistry.register(new SRPPressureSource(0.8D));
        LOGGER.info("  [OK] SRP pressure source registered");
        
        // SCP pressure source (SCP entity count and manifestations)
        PressureRegistry.register(new SCPPressureSource(0.6D));
        LOGGER.info("  [OK] SCP pressure source registered");
        
        LOGGER.info("Total pressure sources registered: " + getPressureSourceCount());
    }
    
    /**
     * Initialize all mod integrations
     */
    private static void initializeModIntegrations() {
        LOGGER.info("Initializing mod integrations...");
        
        // SCP-001 Integration
        try {
            SCP001Integration scpIntegration = new SCP001Integration();
            scpIntegration.init();
            if (scpIntegration.isActive()) {
                LOGGER.info("  [OK] SCP-001 integration initialized");
            } else {
                LOGGER.info("  [SKIP] SCP-001 not detected");
            }
        } catch (Throwable t) {
            LOGGER.warn("  [ERR] Failed to initialize SCP-001 integration: " + t.getMessage());
        }
        
        // SRP Integration
        try {
            SRPIntegration srpIntegration = new SRPIntegration();
            srpIntegration.init();
            if (srpIntegration.isActive()) {
                LOGGER.info("  [OK] SRP integration initialized");
            } else {
                LOGGER.info("  [SKIP] SRP not detected");
            }
        } catch (Throwable t) {
            LOGGER.warn("  [ERR] Failed to initialize SRP integration: " + t.getMessage());
        }
        
        // GameStages Integration
        try {
            GameStagesIntegration gameStagesIntegration = new GameStagesIntegration();
            gameStagesIntegration.init();
            if (gameStagesIntegration.isActive()) {
                LOGGER.info("  [OK] GameStages integration initialized");
            } else {
                LOGGER.info("  [SKIP] GameStages not detected");
            }
        } catch (Throwable t) {
            LOGGER.warn("  [ERR] Failed to initialize GameStages integration: " + t.getMessage());
        }
    }
    
    /**
     * Get current number of registered pressure sources (for logging)
     */
    private static int getPressureSourceCount() {
        try {
            // Access the PressureRegistry sources list via reflection
            java.lang.reflect.Field field = PressureRegistry.class.getDeclaredField("sources");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.List<net.vas.rdpcore.util.IRdpPressureSource> sources = 
                (java.util.List<net.vas.rdpcore.util.IRdpPressureSource>) field.get(null);
            return sources != null ? sources.size() : 0;
        } catch (Throwable t) {
            return 0;
        }
    }
}
