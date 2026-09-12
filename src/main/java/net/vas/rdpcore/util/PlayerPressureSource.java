package net.vas.rdpcore.util;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.util.math.MathHelper;
import net.vas.rdpcore.api.RDPAPI;
import net.vas.rdpcore.config.RDPConfig;
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
        try {
            MinecraftServer server = RDPAPI.getMinecraftServer();
            if (server == null) return 0.0D;

            int count = getPressureCount(world, region, server);
            return count * perPlayerPressure;
        } catch (Throwable t) {
            return 0.0D;
        }
    }

    private static int getPressureCount(World world, RDPRegion region, MinecraftServer server) {
        int regionBlockSize = RDPConfig.REGION_SIZE_CHUNKS * 16;
        int minX = region.getRegionX() * regionBlockSize;
        int minZ = region.getRegionZ() * regionBlockSize;
        int maxX = minX + regionBlockSize;
        int maxZ = minZ + regionBlockSize;
        int count = 0;
        for (EntityPlayerMP player : server.getPlayerList().getPlayers()) {
            if (player == null || player.world != world) continue;

            int px = MathHelper.floor(player.posX);
            int pz = MathHelper.floor(player.posZ);
            if (px >= minX && px < maxX && pz >= minZ && pz < maxZ) {
                count++;
            }
        }
        return count;
    }
}
