package net.vas.rdpcore.util;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.vas.rdpcore.region.RDPRegion;

/**
 * Pressure source based on nearby player activity in a region.
 */
public class PlayerPressureSource implements IRdpPressureSource {

    private final double perPlayerPressure;

    public PlayerPressureSource(double perPlayerPressure) {
        this.perPlayerPressure = perPlayerPressure;
    }

    @Override
    public double getPressure(World world, Object context) {
        if (!(context instanceof RDPRegion)) return 0.0D;
        RDPRegion region = (RDPRegion) context;
        if (world.isRemote) return 0.0D;
        WorldServer ws = (WorldServer) world;
        try {
            // Get server from RDPServerContext (no reflection hacks)
            MinecraftServer server = net.vas.rdpcore.api.RDPAPI.getMinecraftServer();
            if (server == null) return 0.0D;
            
            int regionBlockSize = net.vas.rdpcore.config.RDPConfig.REGION_SIZE_CHUNKS * 16;
            int minX = region.getRegionX() * regionBlockSize;
            int minZ = region.getRegionZ() * regionBlockSize;
            int maxX = minX + regionBlockSize;
            int maxZ = minZ + regionBlockSize;
            int count = 0;
            for (Object o : server.getPlayerList().getPlayers()) {
                if (!(o instanceof EntityPlayerMP)) continue;
                EntityPlayerMP p = (EntityPlayerMP) o;
                if (p.world != world) continue;
                int px = (int)p.posX;
                int pz = (int)p.posZ;
                if (px >= minX && px <= maxX && pz >= minZ && pz <= maxZ) count++;
            }
            return count * perPlayerPressure;
        } catch (Throwable t) {
            return 0.0D;
        }
    }
}

