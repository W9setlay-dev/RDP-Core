package net.vas.rdpcore.anomaly.interdimensional;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.world.WorldServer;
import net.vas.rdpcore.config.RDPConfig;
import net.vas.rdpcore.mutation.MutationCoordinator;
import net.vas.rdpcore.mutation.MutationRequest;
import net.vas.rdpcore.world.RDPWorldState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Server-side coordinator for persistent dimensional collisions.
 *
 * <p>It only schedules bounded region requests. Physical block changes remain
 * owned by the optional Chunk Rewriter integration.</p>
 */
public final class InterdimensionalAnomalyManager {
    private static final Logger LOGGER = LogManager.getLogger("rdpcore");
    private static final AnomalyProfileRegistry PROFILES = new AnomalyProfileRegistry();
    private static final AnomalyConfig CORE_CONFIG = AnomalyConfig.defaults();

    private InterdimensionalAnomalyManager() { }

    public static AnomalyProfileRegistry getProfiles() {
        return PROFILES;
    }

    public static InterdimensionalAnomaly create(WorldServer world, String profileId,
                                                 double x, double y, double z, long timestamp) {
        if (world == null || profileId == null) return null;
        AnomalyProfile profile = PROFILES.get(profileId);
        if (profile == null || profile.getHostDimension() != world.provider.getDimension()) {
            LOGGER.warn("Rejected anomaly profile {} for host dimension {}", profileId,
                world.provider.getDimension());
            return null;
        }
        if (RDPConfig.INTERDIMENSIONAL_MAX_ACTIVE <= 0) return null;
        RDPWorldState state = net.vas.rdpcore.api.RDPAPI.getWorldState(world);
        if (state == null || state.getInterdimensionalAnomalies().size()
                >= RDPConfig.INTERDIMENSIONAL_MAX_ACTIVE) return null;
        long seed = world.getSeed() ^ UUID.randomUUID().getMostSignificantBits()
            ^ Double.doubleToLongBits(x) ^ Double.doubleToLongBits(z);
        InterdimensionalAnomaly anomaly = InterdimensionalAnomaly.create(
            UUID.randomUUID(), profile, x, y, z, seed, timestamp, CORE_CONFIG);
        state.addInterdimensionalAnomaly(anomaly);
        LOGGER.info("Created interdimensional anomaly {} profile={} host={} source={}",
            anomaly.getId(), profileId, anomaly.getHostDimension(), anomaly.getSourceDimension());
        return anomaly;
    }

    public static void tick(WorldServer world, RDPWorldState state, long tick) {
        if (world == null || state == null || !RDPConfig.ENABLE_INTERDIMENSIONAL_ANOMALIES) return;
        int cycleBudget = Math.max(0, RDPConfig.INTERDIMENSIONAL_CHUNKS_PER_CYCLE);
        int scheduled = 0;
        for (InterdimensionalAnomaly anomaly : state.getInterdimensionalAnomalies().values()) {
            try {
                AnomalyProfile profile = PROFILES.get(anomaly.getProfileId());
                if (profile == null || anomaly.isClosed()) continue;
                AnomalyProgression.advance(anomaly, profile, 0.0D, tick, CORE_CONFIG);
                Set<Long> chunks = AnomalyGeometry.affectedChunks(anomaly, CORE_CONFIG);
                Set<Long> fresh = new HashSet<Long>(chunks);
                fresh.removeAll(anomaly.getAffectedChunks());
                if (!fresh.isEmpty() && scheduled < cycleBudget && RDPConfig.ENABLE_CHUNK_REWRITING) {
                    int radius = Math.max(0, (int) Math.ceil(anomaly.getRadius() / 16.0D));
                    MutationRequest request = MutationRequest.builder()
                        .center((int) Math.floor(anomaly.getCenterX() / 16.0D),
                            (int) Math.floor(anomaly.getCenterZ() / 16.0D))
                        .world(world)
                        .radius(Math.min(radius, RDPConfig.INTERDIMENSIONAL_MAX_RADIUS_CHUNKS))
                        .intensity((float) anomaly.getPressure())
                        .priority(60)
                        .budget(Math.min(RDPConfig.INTERDIMENSIONAL_BLOCKS_PER_CYCLE,
                            RDPConfig.MUTATION_CRITICAL_BUDGET))
                        .profile(profile.getId())
                        .cause("INTERDIMENSIONAL_" + profile.getId())
                        .dimension(world.provider.getDimensionType().getName())
                        .build();
                    MutationCoordinator.getInstance().queueMutation(request, 60);
                    anomaly.trackAffectedChunks(fresh);
                    scheduled++;
                }
                if (anomaly.getStage() == AnomalyStage.COLLAPSED
                        && RDPConfig.INTERDIMENSIONAL_DECAY_ENABLED) {
                    anomaly.close(tick);
                }
            } catch (RuntimeException ex) {
                LOGGER.warn("Failed processing interdimensional anomaly {} profile={}: {}",
                    anomaly.getId(), anomaly.getProfileId(), ex.getMessage());
            }
        }
    }
}
