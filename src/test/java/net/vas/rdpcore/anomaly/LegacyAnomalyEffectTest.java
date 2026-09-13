package net.vas.rdpcore.anomaly;

import net.vas.rdpcore.region.RDPRegion;
import net.vas.rdpcore.world.RDPWorldState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class LegacyAnomalyEffectTest {

    @Test
    void spatialEffectCreatesPressureInCorrectNegativeRegion() {
        RDPWorldState state = new RDPWorldState(null);
        net.vas.rdpcore.anomaly.spatial.SpatialAnomaly anomaly =
            new net.vas.rdpcore.anomaly.spatial.SpatialAnomaly(-1, 64, -1, 1.0D);

        anomaly.applyEffect(state);

        RDPRegion region = state.getRegion(-1, -1);
        assertSame(region, state.getOrCreateRegion(-16, -16));
        assertEquals(0.02D, region.getPressure(), 1.0e-12D);
        assertEquals(13, anomaly.getAffectedChunkCount());
    }

    @Test
    void temporalAndDimensionalEffectsRemainBounded() {
        RDPWorldState state = new RDPWorldState(null);
        net.vas.rdpcore.anomaly.temporal.TemporalAnomaly temporal =
            new net.vas.rdpcore.anomaly.temporal.TemporalAnomaly(0, 64, 0, 4.0D);
        net.vas.rdpcore.anomaly.dimensional.DimensionalAnomaly dimensional =
            new net.vas.rdpcore.anomaly.dimensional.DimensionalAnomaly(0, 64, 0, 4.0D);

        temporal.tick();
        dimensional.tick();
        temporal.applyEffect(state);
        dimensional.applyEffect(state);

        assertEquals(4.0D, temporal.getTimeMultiplier(), 1.0e-12D);
        assertEquals(10, dimensional.getRiftSize());
        assertEquals(0, dimensional.getSpilledEntityCount());
        assertEquals(0.045D, state.getRegion(0, 0).getPressure(), 1.0e-12D);
    }

    @Test
    void effectsAreSafeForNullWorldState() {
        new net.vas.rdpcore.anomaly.spatial.SpatialAnomaly(0, 0, 0, 1.0D).applyEffect(null);
        new net.vas.rdpcore.anomaly.temporal.TemporalAnomaly(0, 0, 0, 1.0D).applyEffect(null);
        new net.vas.rdpcore.anomaly.dimensional.DimensionalAnomaly(0, 0, 0, 1.0D).applyEffect(null);
    }
}
