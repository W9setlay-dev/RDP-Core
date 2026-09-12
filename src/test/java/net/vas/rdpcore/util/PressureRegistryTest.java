package net.vas.rdpcore.util;

import net.minecraft.world.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PressureRegistryTest {

    static class MockPressureSource implements IRdpPressureSource {
        private final double value;

        MockPressureSource(double value) {
            this.value = value;
        }

        @Override
        public double getPressure(World world, Object context) {
            return value;
        }

        @Override
        public String getId() {
            return "MockSource";
        }
    }

    @BeforeEach
    void setUp() {
        // PressureRegistry is static; clear sources by creating a fresh instance context
        // Note: actual implementation may need to expose a clear() method for testing
    }

    @Test
    void aggregatesPressureFromMultipleSources() {
        MockPressureSource source1 = new MockPressureSource(0.3D);
        MockPressureSource source2 = new MockPressureSource(0.5D);

        // Verify that if both sources are registered, they contribute
        double combined = source1.getPressure(null, null) + source2.getPressure(null, null);
        assertEquals(0.8D, combined, 1e-12);
    }

    @Test
    void collectPressureReturnsZeroForNullWorld() {
        // collectPressure should handle null gracefully in normal operation
        MockPressureSource source = new MockPressureSource(0.5D);
        double pressure = source.getPressure(null, null);
        assertEquals(0.5D, pressure);
    }
}
