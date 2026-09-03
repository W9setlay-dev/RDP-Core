package net.vas.rdpcore;

import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Event handlers for Forge world events.
 * All handlers are server-side only since RDP is a server-side mod.
 * 
 * No longer performs reflection hacks or fragile server initialization.
 * Server context is managed by RDPServerLifecycleManager.
 */
public class RDPWorldEventHandler {
    
    private static final Logger LOGGER = LogManager.getLogger("rdpcore");

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onWorldLoad(WorldEvent.Load event) {
        World world = event.getWorld();
        if (world.isRemote) {
            return;
        }
        
        try {
            net.vas.rdpcore.server.RDPServerContext context = 
                net.vas.rdpcore.server.RDPServerContext.getInstance();
            
            // Register this world with the server context
            if (world instanceof WorldServer) {
                context.registerWorld((WorldServer) world);
            }
            
            // Load saved data
            net.minecraft.world.storage.WorldSavedData d = 
                ((WorldServer)world).getPerWorldStorage().getOrLoadData(
                    net.vas.rdpcore.world.RDPWorldSavedData.class, 
                    net.vas.rdpcore.world.RDPWorldSavedData.DATA_NAME);
            
            net.vas.rdpcore.world.RDPWorldState state;
            if (d instanceof net.vas.rdpcore.world.RDPWorldSavedData) {
                state = ((net.vas.rdpcore.world.RDPWorldSavedData)d).getState();
            } else {
                state = new net.vas.rdpcore.world.RDPWorldState(world);
            }
            
            net.vas.rdpcore.api.RDPAPI.registerWorldState(world, state);
            LOGGER.info("[RDP] World state loaded/registered for: {}", world.getWorldInfo().getWorldName());
        } catch (Throwable t) {
            LOGGER.warn("[RDP] Failed to load world state: {}", t.getMessage(), t);
        }
    }

    @SubscribeEvent
    public void onWorldSave(WorldEvent.Save event) {
        World world = event.getWorld();
        if (world.isRemote) {
            return;
        }
        
        net.vas.rdpcore.world.RDPWorldState state = net.vas.rdpcore.api.RDPAPI.getWorldState(world);
        if (state != null) {
            try {
                net.minecraft.world.storage.WorldSavedData d = 
                    ((WorldServer)world).getPerWorldStorage().getOrLoadData(
                        net.vas.rdpcore.world.RDPWorldSavedData.class, 
                        net.vas.rdpcore.world.RDPWorldSavedData.DATA_NAME);
                
                net.vas.rdpcore.world.RDPWorldSavedData sd;
                if (d instanceof net.vas.rdpcore.world.RDPWorldSavedData) {
                    sd = (net.vas.rdpcore.world.RDPWorldSavedData)d;
                } else {
                    sd = new net.vas.rdpcore.world.RDPWorldSavedData(world);
                    ((WorldServer)world).getPerWorldStorage().setData(
                        net.vas.rdpcore.world.RDPWorldSavedData.DATA_NAME, sd);
                }
                
                sd.getState().deserializeNBT(state.serializeNBT());
                sd.markDirty();
                LOGGER.debug("[RDP] World state saved for: {}", world.getWorldInfo().getWorldName());
            } catch (Throwable t) {
                LOGGER.warn("[RDP] Failed to persist world state: {}", t.getMessage(), t);
            }
        }
    }
    
    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        World world = event.getWorld();
        if (world.isRemote) {
            return;
        }
        
        try {
            if (world instanceof WorldServer) {
                net.vas.rdpcore.server.RDPServerContext context = 
                    net.vas.rdpcore.server.RDPServerContext.getInstance();
                int dimensionId = world.provider.getDimension();
                context.unregisterWorld(dimensionId);
                
                // Unregister from RDPAPI as well
                net.vas.rdpcore.api.RDPAPI.unregisterWorldState(world);
                LOGGER.info("[RDP] World unloaded: dimension {}", dimensionId);
            }
        } catch (Throwable t) {
            LOGGER.warn("[RDP] Failed during world unload: {}", t.getMessage(), t);
        }
    }
}

