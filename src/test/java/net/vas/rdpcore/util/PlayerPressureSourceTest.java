package net.vas.rdpcore.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerPressureSourceTest {

    @Test
    void pressureScalesLinearlyWithPlayerCount() {
        assertEquals(0.0D, PlayerPressureSource.pressureForPlayerCount(0, 0.05D));
        assertEquals(0.15D, PlayerPressureSource.pressureForPlayerCount(3, 0.05D), 1e-12);
    }

    @Test
    void regionUsesHalfOpenBounds() {
        assertTrue(PlayerPressureSource.isInsideRegion(0.0D, 0.0D, 0, 0, 16));
        assertTrue(PlayerPressureSource.isInsideRegion(255.999D, 255.999D, 0, 0, 16));
        assertFalse(PlayerPressureSource.isInsideRegion(256.0D, 0.0D, 0, 0, 16));
        assertTrue(PlayerPressureSource.isInsideRegion(256.0D, 0.0D, 1, 0, 16));
    }

    @Test
    void regionClassificationHandlesNegativeCoordinates() {
        assertTrue(PlayerPressureSource.isInsideRegion(-0.001D, -0.001D, -1, -1, 16));
        assertTrue(PlayerPressureSource.isInsideRegion(-256.0D, -0.001D, -1, -1, 16));
        assertFalse(PlayerPressureSource.isInsideRegion(-256.001D, -0.001D, -1, -1, 16));
        assertTrue(PlayerPressureSource.isInsideRegion(-256.0D, -256.0D, -1, -1, 16));
    }
}
