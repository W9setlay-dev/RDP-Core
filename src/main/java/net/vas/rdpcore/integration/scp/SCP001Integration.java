package net.vas.rdpcore.integration.scp;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraft.world.WorldServer;
import net.vas.rdpcore.RDPCore;
import net.vas.rdpcore.api.RDPAPI;
import net.vas.rdpcore.core.GlobalRDPLevel;
import net.vas.rdpcore.event.RDPEvents;
import net.vas.rdpcore.integration.ModIntegration;
import net.vas.rdpcore.world.RDPWorldState;

/**
 * Integration adapter for SCP-001 Controller and SCP Project Anomalous.
 * Synchronizes RDP progression with SCP manifestations and scenario selection.
 */
public class SCP001Integration extends ModIntegration {
    
    private GlobalRDPLevel.RDPStage lastKnownStage = null;
    private int tickCounter = 0;
    
    public SCP001Integration() {
        super("scp001projectanomalous");
    }
    
    @Override
    public boolean checkModLoaded() {
        try {
            Class.forName("net.scp.entity.EntitySCPBase");
            this.isLoaded = true;
            return true;
        } catch (ClassNotFoundException e1) {
            try {
                Class.forName("rdp.scp001.gate.SCP001AchievementGate");
                this.isLoaded = true;
                return true;
            } catch (ClassNotFoundException e2) {
                this.isLoaded = false;
                return false;
            }
        }
    }
    
    @Override
    public void init() {
        if (checkModLoaded()) {
            log("SCP-001 detected. Initializing integration...");
            MinecraftForge.EVENT_BUS.register(this);
            MinecraftForge.EVENT_BUS.register(new SCP001EventListener());
            log("SCP-001 integration initialized.");
        } else {
            log("SCP-001 not found. Integration disabled.");
        }
    }
    
    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !isLoaded) return;
        
        tickCounter++;
        if (tickCounter >= 20) {
            tickCounter = 0;
            checkStageProgression();
        }
    }
    
    private void checkStageProgression() {
        try {
            net.minecraft.server.MinecraftServer server = RDPAPI.getMinecraftServer();
            if (server == null) return;
            
            for (WorldServer world : server.worlds) {
                if (world == null) continue;
                
                RDPWorldState state = RDPAPI.getWorldState(world);
                if (state == null) continue;
                
                GlobalRDPLevel.RDPStage currentStage = state.getGlobalRDPLevel().getCurrentStage();
                
                if (lastKnownStage != null && lastKnownStage != currentStage) {
                    onRDPStageChanged(world, currentStage);
                }
                lastKnownStage = currentStage;
            }
        } catch (Throwable t) {
            // Ignore errors
        }
    }
    
    private void onRDPStageChanged(WorldServer world, GlobalRDPLevel.RDPStage newStage) {
        log("RDP stage changed to " + newStage.name());
    }
    
    public static class SCP001EventListener {
        @SubscribeEvent
        public void onAnomalySpawn(RDPEvents.AnomalySpawnEvent event) {
        }
        
        @SubscribeEvent
        public void onStageChange(RDPEvents.RDPStageChangeEvent event) {
        }
    }
    
    public double getSCPActivityLevel() {
        return isLoaded ? 0.5D : 0.0D;
    }
    
    protected void log(String message) {
        RDPCore.LOGGER.info("[SCP-001] {}", message);
    }
}
