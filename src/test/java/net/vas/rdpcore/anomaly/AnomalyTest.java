package net.vas.rdpcore.anomaly;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AnomalyTest {

    @Test
    void anomalyAgeIncrementsTick() {
        Anomaly anomaly = new TemporalAnomaly(0, 64, 0, 0.5D);
        assertEquals(0, anomaly.getAge());
        anomaly.tick();
        assertEquals(1, anomaly.getAge());
    }

    @Test
    void anomalyIntensityDecaysFasterThanOne() {
        Anomaly anomaly = new TemporalAnomaly(0, 64, 0, 0.5D);
        double initial = anomaly.getIntensity();
        anomaly.tick();
        double afterTick = anomaly.getIntensity();
        assertTrue(afterTick < initial, "Intensity should decay each tick");
    }

    @Test
    void anomalyBecomesInactiveWhenAgeExceedsMax() {
        Anomaly anomaly = new TemporalAnomaly(0, 64, 0, 0.5D);
        anomaly.maxAge = 5;
        for (int i = 0; i < 6; i++) {
            anomaly.tick();
        }
        assertFalse(anomaly.isActive(), "Anomaly should be inactive after exceeding maxAge");
    }

    @Test
    void anomalyIntensityNeverGoesNegative() {
        Anomaly anomaly = new TemporalAnomaly(0, 64, 0, 0.001D);
        for (int i = 0; i < 1000; i++) {
            anomaly.tick();
        }
        assertTrue(anomaly.getIntensity() >= 0.0D, "Intensity should never be negative");
    }
}
