package net.vas.rdpcore.integration.modpack;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;
import net.vas.rdpcore.api.RDPAPI;
import net.vas.rdpcore.core.GlobalRDPLevel;
import net.vas.rdpcore.event.RDPEvents;
import net.vas.rdpcore.integration.ModIntegration;
import net.vas.rdpcore.world.RDPWorldState;

/**
 * Integration adapter for GameStages.
 * Unlocks/locks game stages based on RDP progression.
 * Provides progression gating tied to reality distortion.
 * 
 * Features:
 * - Automatically unlocks GameStages as RDP progresses
 * - Locks stages if RDP decreases significantly
 * - Per-player stage management
 * - Smooth progression tied to cosmological escalation
 */
public class GameStagesIntegration extends ModIntegration {
    
    private int tickCounter = 0;
    private static final int STAGE_CHECK_INTERVAL = 100; // Ticks between stage checks
    
    // Stage names
    private static final String[] STAGE_NAMES = {
        "rdp_stage_0",    // RDP-0 (0.00-0.09)
        "rdp_stage_i",    // RDP-I (0.10-0.24)
        "rdp_stage_ii",   // RDP-II (0.25-0.39)
        "rdp_stage_iii",  // RDP-III (0.40-0.54)
        "rdp_stage_iv",   // RDP-IV (0.55-0.69)
        "rdp_stage_v",    // RDP-V (0.70-0.79)
        "rdp_stage_vi",   // RDP-VI (0.80-0.84)
        "rdp_stage_vii",  // RDP-VII (0.85-0.89)
        "rdp_stage_viii", // RDP-VIII (0.90-0.96)
        "rdp_stage_x"     // RDP-X (0.97-1.00)
    };
    
    public GameStagesIntegration() {
        super("gamestages");
    }
    
    @Override
    public boolean checkModLoaded() {
        try {
            Class.forName("net.darkhax.gamestages.GameStages");
            this.isLoaded = true;
            return true;
        } catch (ClassNotFoundException e) {
            this.isLoaded = false;
            return false;
        }
    }
    
    @Override
    public void init() {
        if (checkModLoaded()) {
            log("GameStages detected. Initializing integration...");
            MinecraftForge.EVENT_BUS.register(this);
            MinecraftForge.EVENT_BUS.register(new GameStagesEventListener());
            log("GameStages integration initialized. Stages will unlock as RDP progresses.");
        } else {
            log("GameStages not found. Integration disabled.");
        }
    }
    
    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !isLoaded) return;
        
        tickCounter++;
        
        // Check and update player stages every 100 ticks
        if (tickCounter >= STAGE_CHECK_INTERVAL) {
            tickCounter = 0;
            updatePlayerStages();
        }
    }
    
    /**
     * Update game stages for all players based on global RDP level
     */
    private void updatePlayerStages() {
        try {
            net.minecraft.server.MinecraftServer server = RDPAPI.getMinecraftServer();
            if (server == null) return;
            
            for (WorldServer world : server.worlds) {
                if (world == null) continue;
                
                RDPWorldState state = RDPAPI.getWorldState(world);
                if (state == null) continue;
                
                GlobalRDPLevel.RDPStage currentStage = state.getGlobalRDPLevel().getCurrentStage();
                
                // Update all players in this world
                for (Object playerObj : world.playerEntities) {
                    if (playerObj instanceof EntityPlayerMP) {
                        EntityPlayerMP player = (EntityPlayerMP) playerObj;
                        syncPlayerStages(player, currentStage);
                    }
                }
            }
        } catch (Throwable t) {
            // Silently ignore errors to prevent integration from breaking simulation
        }
    }
    
    /**
     * Synchronize a player's GameStages with their current RDP stage
     */
    private void syncPlayerStages(EntityPlayerMP player, GlobalRDPLevel.RDPStage stage) {
        try {
            // Get the current stage index (0-9)
            int stageIndex = stage.ordinal();
            
            // Unlock all stages up to and including current stage
            for (int i = 0; i <= stageIndex && i < STAGE_NAMES.length; i++) {
                unlockStage(player, STAGE_NAMES[i]);
            }
            
            // Lock all stages beyond current stage
            for (int i = stageIndex + 1; i < STAGE_NAMES.length; i++) {
                lockStage(player, STAGE_NAMES[i]);
            }
        } catch (Throwable t) {
            // Silently ignore
        }
    }
    
    /**
     * Unlock a game stage for a player using reflection
     */
    public void unlockStage(Object player, String stageName) {
        if (!isLoaded) return;
        
        try {
            if (!(player instanceof EntityPlayerMP)) return;
            EntityPlayerMP entityPlayer = (EntityPlayerMP) player;
            
            // Access GameStages API via reflection
            Class<?> gameStagesClass = Class.forName("net.darkhax.gamestages.GameStages");
            java.lang.reflect.Method unlockedStagesMethod = gameStagesClass.getMethod("getUnlockedStages", net.minecraft.entity.player.EntityPlayer.class);
            Object stageCollection = unlockedStagesMethod.invoke(null, entityPlayer);
            
            if (stageCollection != null) {
                // Try to add the stage to unlocked collection
                java.lang.reflect.Method addMethod = stageCollection.getClass().getMethod("add", Object.class);
                if (addMethod != null) {
                    addMethod.invoke(stageCollection, stageName);
                }
            }
        } catch (Throwable t) {
            // Silently ignore - GameStages API may differ in different versions
        }
    }
    
    /**
     * Lock a game stage for a player using reflection
     */
    public void lockStage(Object player, String stageName) {
        if (!isLoaded) return;
        
        try {
            if (!(player instanceof EntityPlayerMP)) return;
            EntityPlayerMP entityPlayer = (EntityPlayerMP) player;
            
            // Access GameStages API via reflection
            Class<?> gameStagesClass = Class.forName("net.darkhax.gamestages.GameStages");
            java.lang.reflect.Method unlockedStagesMethod = gameStagesClass.getMethod("getUnlockedStages", net.minecraft.entity.player.EntityPlayer.class);
            Object stageCollection = unlockedStagesMethod.invoke(null, entityPlayer);
            
            if (stageCollection != null) {
                // Try to remove the stage from unlocked collection
                java.lang.reflect.Method removeMethod = stageCollection.getClass().getMethod("remove", Object.class);
                if (removeMethod != null) {
                    removeMethod.invoke(stageCollection, stageName);
                }
            }
        } catch (Throwable t) {
            // Silently ignore
        }
    }
    
    /**
     * Check if player has unlocked a stage
     */
    public boolean hasStage(Object player, String stageName) {
        if (!isLoaded) return true; // Default to true if mod not loaded
        
        try {
            if (!(player instanceof EntityPlayerMP)) return false;
            EntityPlayerMP entityPlayer = (EntityPlayerMP) player;
            
            Class<?> gameStagesClass = Class.forName("net.darkhax.gamestages.GameStages");
            java.lang.reflect.Method hasStageMethod = gameStagesClass.getMethod("hasStage", net.minecraft.entity.player.EntityPlayer.class, String.class);
            Object result = hasStageMethod.invoke(null, entityPlayer, stageName);
            return result instanceof Boolean && (Boolean) result;
        } catch (Throwable t) {
            return false;
        }
    }
    
    /**
     * Inner class to listen for RDP events and trigger stage updates
     */
    public static class GameStagesEventListener {
        @SubscribeEvent
        public void onStageChange(RDPEvents.RDPStageChangeEvent event) {
            // Stage changes handled in main integration class via tick synchronization
            // This listener can be extended for custom behavior on stage transitions
        }
    }
}
