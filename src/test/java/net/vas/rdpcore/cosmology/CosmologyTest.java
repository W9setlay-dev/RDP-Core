package net.vas.rdpcore.cosmology;

import net.minecraft.util.ResourceLocation;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CosmologyTest {
    @Test public void easingIsBoundedAndMonotonic() {
        for (Easing easing : Easing.values()) {
            double previous = -1.0D;
            for (int i = 0; i <= 100; i++) {
                double value = easing.apply(i / 100.0D);
                assertTrue(value >= 0.0D && value <= 1.0D);
                assertTrue(value >= previous);
                previous = value;
            }
        }
    }
    @Test public void definitionRejectsInvalidRanges() {
        assertThrows(IllegalArgumentException.class, () -> CelestialObjectDefinition
            .builder("bad", new ResourceLocation("rdpcore", "bad")).alpha(2.0D).build());
        assertThrows(IllegalArgumentException.class, () -> CelestialObjectDefinition
            .builder("bad", new ResourceLocation("rdpcore", "bad")).rdp(0.8D, 0.2D).build());
    }
    @Test public void stableSeedIsDeterministic() {
        assertEquals(CosmologyMath.stableSeed(1L, 0, "event", "object"),
            CosmologyMath.stableSeed(1L, 0, "event", "object"));
        assertNotEquals(CosmologyMath.stableSeed(1L, 0, "event", "object"),
            CosmologyMath.stableSeed(2L, 0, "event", "object"));
    }
}
