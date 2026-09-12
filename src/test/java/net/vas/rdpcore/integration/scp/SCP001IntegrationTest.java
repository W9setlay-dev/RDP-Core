package net.vas.rdpcore.integration.scp;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SCP001IntegrationTest {
    @Test
    void inactiveControllerProducesNoActivity() {
        assertEquals(0.0D, SCP001Integration.calculateActivity(true, false, true, true, 5));
        assertEquals(0.0D, SCP001Integration.calculateActivity(false, true, true, true, 5));
    }

    @Test
    void activityReflectsControllerStateAndEntityCount() {
        assertEquals(0.125D, SCP001Integration.calculateActivity(true, true, false, false, 5));
        assertEquals(0.25D, SCP001Integration.calculateActivity(true, true, true, false, 5));
        assertEquals(0.5D, SCP001Integration.calculateActivity(true, true, true, true, 5));
    }

    @Test
    void activityIsCapped() {
        assertEquals(1.0D, SCP001Integration.calculateActivity(true, true, true, true, 100));
    }
}
