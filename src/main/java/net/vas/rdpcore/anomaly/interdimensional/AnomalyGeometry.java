package net.vas.rdpcore.anomaly.interdimensional;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public final class AnomalyGeometry {
    private AnomalyGeometry() { }

    public static Set<Long> affectedChunks(InterdimensionalAnomaly anomaly, AnomalyConfig config) {
        int centerChunkX = floorChunk(anomaly.getCenterX());
        int centerChunkZ = floorChunk(anomaly.getCenterZ());
        int chunkRadius = (int) Math.ceil(anomaly.getRadius() / 16.0D);
        LinkedHashSet<Long> result = new LinkedHashSet<Long>();
        for (int x = centerChunkX - chunkRadius; x <= centerChunkX + chunkRadius; x++) {
            for (int z = centerChunkZ - chunkRadius; z <= centerChunkZ + chunkRadius; z++) {
                double dx = (x + 0.5D) * 16.0D - anomaly.getCenterX();
                double dz = (z + 0.5D) * 16.0D - anomaly.getCenterZ();
                double edge = anomaly.getRadius() * (0.90D + 0.20D * unitNoise(anomaly.getSeed(), x, z));
                if (dx * dx + dz * dz <= edge * edge) {
                    result.add(chunkKey(x, z));
                }
            }
        }
        result.add(chunkKey(centerChunkX, centerChunkZ));
        if (result.size() > config.getMaxAffectedChunks()) {
            throw new IllegalStateException("anomaly affected chunk safety cap exceeded");
        }
        return Collections.unmodifiableSet(result);
    }

    private static int floorChunk(double coordinate) {
        return (int) Math.floor(coordinate / 16.0D);
    }

    private static double unitNoise(long seed, int x, int z) {
        long value = seed ^ (x * 341873128712L) ^ (z * 132897987541L);
        value ^= value >>> 33;
        value *= 0xff51afd7ed558ccdl;
        value ^= value >>> 33;
        return (value & Long.MAX_VALUE) / (double) Long.MAX_VALUE;
    }

    public static long chunkKey(int chunkX, int chunkZ) {
        return ((long) chunkX << 32) ^ (chunkZ & 0xffffffffL);
    }
}
