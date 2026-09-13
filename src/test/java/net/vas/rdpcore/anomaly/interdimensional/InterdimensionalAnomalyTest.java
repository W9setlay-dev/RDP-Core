package net.vas.rdpcore.anomaly.interdimensional;

import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.NBTTagCompound;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InterdimensionalAnomalyTest {
    private final AnomalyConfig config = AnomalyConfig.defaults();
    private final AnomalyProfileRegistry profiles = new AnomalyProfileRegistry();

    @Test
    void rejectsUnsafeConfiguration() {
        assertThrows(IllegalArgumentException.class,
                () -> new AnomalyConfig(AnomalyConfig.MAX_RADIUS_CAP + 1.0D, 1, .1, .2, .3));
        assertThrows(IllegalArgumentException.class,
                () -> new AnomalyConfig(1.0D, AnomalyConfig.MAX_AFFECTED_CHUNKS_CAP + 1, .1, .2, .3));
        assertThrows(IllegalArgumentException.class,
                () -> new AnomalyConfig(1.0D, 1, .4, .2, .8));
    }

    @Test
    void netherLeakProfileIsRegistered() {
        AnomalyProfile profile = profiles.get(AnomalyProfileRegistry.NETHER_LEAK);
        assertNotNull(profile);
        assertEquals(-1, profile.getSourceDimension());
        assertEquals(0, profile.getHostDimension());
    }

    @Test
    void geometryIsDeterministicAndOrderIndependent() {
        InterdimensionalAnomaly anomaly = InterdimensionalAnomaly.create(
                UUID.randomUUID(), profiles.get(AnomalyProfileRegistry.NETHER_LEAK),
                128.0D, 64.0D, -32.0D, 12345L, 10L, config);
        Set<Long> first = AnomalyGeometry.affectedChunks(anomaly, config);
        Set<Long> second = AnomalyGeometry.affectedChunks(anomaly, config);
        assertEquals(first, second);
        assertFalse(first.isEmpty());
    }

    @Test
    void pressureAdvancesStagesAndLifecycle() {
        InterdimensionalAnomaly anomaly = InterdimensionalAnomaly.create(
                UUID.randomUUID(), profiles.get(AnomalyProfileRegistry.NETHER_LEAK),
                0.0D, 64.0D, 0.0D, 5L, 1L, config);
        AnomalyProgression.advance(anomaly, profiles.get(AnomalyProfileRegistry.NETHER_LEAK),
                .30D, 2L, config);
        assertEquals(AnomalyStage.UNSTABLE, anomaly.getStage());
        assertEquals(AnomalyLifecycle.ACTIVE, anomaly.getLifecycle());
        AnomalyProgression.advance(anomaly, profiles.get(AnomalyProfileRegistry.NETHER_LEAK),
                .70D, 3L, config);
        assertEquals(AnomalyStage.COLLAPSED, anomaly.getStage());
        anomaly.close(4L);
        assertEquals(AnomalyLifecycle.CLOSED, anomaly.getLifecycle());
    }

    @Test
    void affectedChunkTrackingIsBounded() {
        AnomalyConfig small = new AnomalyConfig(32.0D, 2, .1D, .2D, .3D);
        InterdimensionalAnomaly anomaly = InterdimensionalAnomaly.create(
                UUID.randomUUID(), profiles.get(AnomalyProfileRegistry.NETHER_LEAK),
                0.0D, 64.0D, 0.0D, 1L, 1L, small);
        anomaly.trackAffectedChunk(1L);
        anomaly.trackAffectedChunk(2L);
        anomaly.trackAffectedChunk(3L);
        assertEquals(2, anomaly.getAffectedChunks().size());
    }

    @Test
    void nbtRoundTripPreservesState() {
        InterdimensionalAnomaly original = InterdimensionalAnomaly.create(
                UUID.randomUUID(), profiles.get(AnomalyProfileRegistry.NETHER_LEAK),
                10.5D, 70.0D, -2.5D, 77L, 100L, config);
        original.applyPressure(.7D, 120L, config);
        original.trackAffectedChunk(AnomalyGeometry.chunkKey(1, -1));
        NBTTagCompound tag = original.serializeNBT();
        InterdimensionalAnomaly restored = InterdimensionalAnomaly.fromNBT(tag);
        assertEquals(original.getId(), restored.getId());
        assertEquals(original.getSeed(), restored.getSeed());
        assertEquals(original.getProfileId(), restored.getProfileId());
        assertEquals(original.getStage(), restored.getStage());
        assertEquals(original.getLifecycle(), restored.getLifecycle());
        assertEquals(original.getAffectedChunks(), restored.getAffectedChunks());
        assertEquals(original.getPressure(), restored.getPressure(), 1e-12);
    }
}
