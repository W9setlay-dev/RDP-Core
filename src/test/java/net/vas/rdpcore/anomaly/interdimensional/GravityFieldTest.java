package net.vas.rdpcore.anomaly.interdimensional;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GravityFieldTest {
    private final AnomalyConfig anomalyConfig = AnomalyConfig.defaults();

    private InterdimensionalAnomaly active(long created, double pressure) {
        InterdimensionalAnomaly a = InterdimensionalAnomaly.create(UUID.randomUUID(),
            new AnomalyProfile("test", -1, 0, 8, 32, 0, 0), 0, 64, 0, 44, created, anomalyConfig);
        a.applyPressure(pressure, created + 1, anomalyConfig);
        return a;
    }

    @Test
    void gateReportsIndependentReasonsAndBuildup() {
        GravityFieldConfig c = GravityFieldConfig.defaults();
        c.activationDelayTicks = 10;
        c.buildupTicks = 100;
        c.pressureScaling = false;
        InterdimensionalAnomaly a = active(0, .8);
        assertEquals(GravityGateResult.Reason.INACTIVE_GRACE_PERIOD,
            GravityGateEvaluator.evaluate(a, c, 0, null, 5).getReason());
        GravityGateResult result = GravityGateEvaluator.evaluate(a, c, 0, null, 60);
        assertTrue(result.isActive());
        assertEquals(.5, result.getInfluence(), .01);
        c.allowedProfiles.add("other");
        assertEquals(GravityGateResult.Reason.INACTIVE_PROFILE,
            GravityGateEvaluator.evaluate(a, c, 0, null, 200).getReason());
    }

    @Test
    void falloffIsBoundedAndDeterministic() {
        for (GravityFalloff f : GravityFalloff.values()) {
            assertEquals(1.0, f.apply(0, 10), 1e-12);
            if (f != GravityFalloff.INVERSE_SQUARE_CLAMPED) assertEquals(0.0, f.apply(10, 10), 1e-12);
            else assertTrue(f.apply(10, 10) >= 0.0 && f.apply(10, 10) <= 1.0);
            assertTrue(Double.isFinite(f.apply(5, 10)));
        }
    }

    @Test
    void invalidVectorsAndCoreNeverProduceNonFiniteAcceleration() {
        GravityFieldConfig c = GravityFieldConfig.defaults();
        c.directionX = c.directionY = c.directionZ = 0;
        InterdimensionalAnomaly a = active(0, .8);
        assertNotNull(GravityField.calculate(null, a, c, 1));
        assertEquals(0, GravityField.calculate(null, a, c, 1).x, 0);
    }
}
