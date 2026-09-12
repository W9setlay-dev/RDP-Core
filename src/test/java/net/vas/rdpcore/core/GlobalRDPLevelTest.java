package net.vas.rdpcore.core;

import net.vas.rdpcore.config.RDPConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GlobalRDPLevelTest {

    @Test
    void clampsProgressionAndSelectsExpectedStages() {
        GlobalRDPLevel level = new GlobalRDPLevel();

        level.setLevel(-1.0D);
        assertEquals(0.0D, level.getLevel());
        assertSame(GlobalRDPLevel.RDPStage.RDP0, level.getCurrentStage());

        level.setLevel(0.25D);
        assertSame(GlobalRDPLevel.RDPStage.RDPII, level.getCurrentStage());

        level.setLevel(2.0D);
        assertEquals(1.0D, level.getLevel());
        assertSame(GlobalRDPLevel.RDPStage.RDPX, level.getCurrentStage());
    }

    @Test
    void stagePressureScalingIncreasesWithProgression() {
        double previous = 0.0D;
        for (GlobalRDPLevel.RDPStage stage : GlobalRDPLevel.RDPStage.values()) {
            double current = RDPConfig.getStageModifiers(stage).pressureMultiplier;
            assertTrue(current >= previous,
                    "pressure multiplier must not decrease at " + stage);
            previous = current;
        }
        assertEquals(0.5D, RDPConfig.getStageModifiers(GlobalRDPLevel.RDPStage.RDP0).pressureMultiplier);
        assertEquals(3.0D, RDPConfig.getStageModifiers(GlobalRDPLevel.RDPStage.RDPX).pressureMultiplier);
    }
}
