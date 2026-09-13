package net.vas.rdpcore;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.common.MinecraftForge;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import net.vas.rdpcore.config.RDPConfig;
import net.vas.rdpcore.client.CommonProxy;

/**
 * R.D.P. Core - Reality Distortion Phenomenon
 * <p>
 * A world-level simulation framework that unifies major modpack systems into one
 * coherent cosmological-horror phenomenon. RDP is NOT merely another biome, infection,
 * or event mechanic, but rather a fundamental world-state system where reality
 * itself becomes progressively inconsistent.
 * 
 * @author W9setlay
 */
@Mod(
    modid = RDPCore.MOD_ID,
    name = RDPCore.MOD_NAME,
    version = RDPCore.MOD_VERSION,
    acceptedMinecraftVersions = "[1.12.2]",
    updateJSON = ""
)
public class RDPCore {
    private static final String LOG_SEPARATOR =
        "================================================================================";
    
    public static final String MOD_ID = "rdpcore";
    public static final String MOD_NAME = "RDP Core";
    public static final String MOD_VERSION = "1.0.0";
    
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    @net.minecraftforge.fml.common.SidedProxy(
        clientSide = "net.vas.rdpcore.client.ClientProxy",
        serverSide = "net.vas.rdpcore.client.CommonProxy")
    public static CommonProxy PROXY;
    
    @Mod.Instance(MOD_ID)
    public static RDPCore INSTANCE;
    
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER.info(LOG_SEPARATOR);
        LOGGER.info("R.D.P. CORE - REALITY DISTORTION PHENOMENON");
        LOGGER.info("Loading fundamental world-state simulation system...");
        LOGGER.info(LOG_SEPARATOR);
        
        // Initialize configuration system
        RDPConfig.load();
        LOGGER.info("Configuration loaded.");
        
        // Initialize core data structures
        RDPCoreData.init();
        LOGGER.info("Core data structures initialized.");
        PROXY.preInit();
    }
    
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        LOGGER.info("Registering event handlers...");
        
        // Register world-level event handlers
        MinecraftForge.EVENT_BUS.register(new RDPWorldEventHandler());
        MinecraftForge.EVENT_BUS.register(net.vas.rdpcore.RDPTickHandler.getInstance());
        
        // Server lifecycle is managed by @Mod.EventHandler methods below.
        // Do not register the lifecycle manager on the Forge event bus because
        // FMLServerStartingEvent/FMLServerStoppingEvent are not WorldEvent/Forge eventbus types.
        
        // Initialize simulation engine
        RDPSimulationEngine.init();
        
        LOGGER.info("Event handlers registered and simulation engine initialized.");
    }
    
    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        LOGGER.info("Post-initialization: performing integrations...");
        
        // Initialize integrations with other mods
        RDPIntegrationManager.initializeIntegrations();
        
        LOGGER.info("Post-initialization complete. R.D.P. Core ready.");
        LOGGER.info(LOG_SEPARATOR);
    }

    @Mod.EventHandler
    public void serverStarting(net.minecraftforge.fml.common.event.FMLServerStartingEvent event) {
        net.vas.rdpcore.server.RDPServerContext context = net.vas.rdpcore.server.RDPServerContext.getInstance();
        context.initializeWithServer(event.getServer());
        context.markRunning();

        // register server-side commands
        event.registerServerCommand(new net.vas.rdpcore.command.RdpCommand());
        LOGGER.info("[RDP] Server starting lifecycle initialized.");
    }

    @Mod.EventHandler
    public void serverStopping(net.minecraftforge.fml.common.event.FMLServerStoppingEvent event) {
        net.vas.rdpcore.server.RDPServerContext.getInstance().initiateShutdown();
        LOGGER.info("[RDP] Server stopping lifecycle initiated.");
    }
}
