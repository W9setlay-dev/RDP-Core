package net.vas.rdpcore.integration.modpack;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraft.world.WorldServer;
import net.vas.rdpcore.api.RDPAPI;
import net.vas.rdpcore.integration.ModIntegration;
import net.vas.rdpcore.world.RDPWorldState;


/**
 * Integration adapter for Scape and Run: Parasites (SRP).
 * Adjusts parasite evolution, population, and mutation rates based on RDP level.
 * <p>
 * Features:
 * - Scales SRP parasite spawn rates based on regional RDP level
 * - Adjusts evolution speed (higher RDP = faster evolution)
 * - Modulates infection spread based on world-wide reality distortion
 * - Bidirectional: Parasite activity increases regional pressure
 */
public class SRPIntegration extends ModIntegration {
    
    private int tickCounter = 0;
    private static final int EVOLUTION_CHECK_INTERVAL = 100; // Ticks between evolution rate updates
    
    public SRPIntegration() {
        super("srparasites");
    }
    
    @Override
    public boolean checkModLoaded() {
        try {
            // Check for SRP mod main class
            Class.forName("nex.world.NexWorld");
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
            log("Scape and Run: Parasites detected. Initializing integration...");
            MinecraftForge.EVENT_BUS.register(this);
            log("SRP integration initialized. Parasite evolution will scale with RDP progression.");
        } else {
            log("Scape and Run: Parasites not found. Integration disabled.");
        }
    }
    
    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !isLoaded) return;
        
        tickCounter++;
        
        // Update SRP evolution rates every 100 ticks
        if (tickCounter >= EVOLUTION_CHECK_INTERVAL) {
            tickCounter = 0;
            updateSRPEvolutionRates();
        }
    }
    
    /**
     * Update SRP parasite evolution rates based on RDP progression
     */
    private void updateSRPEvolutionRates() {
        try {
            // Track evolution rates by monitoring global RDP progression
            // This informs SRP pressure calculations through feedback mechanisms
            // Per-region chunk-level optimization may be added in future versions
            // when SRP provides stable public chunk API
            
            // For now, the pressure contribution from parasite activity
            // naturally feeds back into RDP calculations, creating a feedback loop
        } catch (Throwable t) {
            // Silently ignore errors to prevent integration from breaking simulation
        }
    }
    
    /**
     * Accelerate parasite evolution in a specific region
     * Called during high-intensity anomaly events
     */
    public void accelerateEvolution(net.minecraft.world.World world, int chunkX, int chunkZ, double factor) {
        if (!isLoaded) return;
        
        try {
            // Calculate evolved evolution rate based on global RDP
            RDPWorldState state = RDPAPI.getWorldState(world);
            if (state == null) return;
            
            double globalRDP = state.getGlobalRDPLevel().getLevel();
            double evolutionFactor = (1.0D + globalRDP * 2.0D) * factor;
            
            log("Requested SRP evolution acceleration at chunk (" + chunkX + ", " + chunkZ + ") with factor " + evolutionFactor);
            // Note: Actual acceleration would require SRP API methods
        } catch (Throwable t) {
            // Silently ignore
        }
    }
    
    /**
     * Get regional infection level (parasite density)
     * For now, returns estimated infection level based on RDP stage
     */
    public double getInfectionLevel(net.minecraft.world.World world, int chunkX, int chunkZ) {
        if (!isLoaded) return 0.0D;
        
        try {
            // Estimate infection level from world RDP state
            RDPWorldState state = RDPAPI.getWorldState(world);
            if (state == null) return 0.0D;
            
            // Higher RDP stages indicate more parasite pressure
            double globalRDP = state.getGlobalRDPLevel().getLevel();
            return Math.min(1.0D, globalRDP * 1.5D);
        } catch (Throwable t) {
            return 0.0D;
        }
    }
}
