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
            return pressureForPlayerCount(count, perPlayerPressure);
        } catch (Throwable t) {
            return 0.0D;
        }
    }

    private static int getPressureCount(World world, RDPRegion region, MinecraftServer server) {
        int count = 0;
        for (EntityPlayerMP player : server.getPlayerList().getPlayers()) {
            if (player == null || player.world != world) continue;

            if (isInsideRegion(player.posX, player.posZ, region.getRegionX(), region.getRegionZ(),
                    RDPConfig.REGION_SIZE_CHUNKS)) {
                count++;
            }
        }
        return count;
    }

    static double pressureForPlayerCount(int count, double pressurePerPlayer) {
        return count * pressurePerPlayer;
    }

    static boolean isInsideRegion(double posX, double posZ, int regionX, int regionZ, int regionSizeChunks) {
        int regionBlockSize = regionSizeChunks * 16;
        int minX = regionX * regionBlockSize;
        int minZ = regionZ * regionBlockSize;
        int maxX = minX + regionBlockSize;
        int maxZ = minZ + regionBlockSize;
        int blockX = MathHelper.floor(posX);
        int blockZ = MathHelper.floor(posZ);
        return blockX >= minX && blockX < maxX && blockZ >= minZ && blockZ < maxZ;
    }
}
