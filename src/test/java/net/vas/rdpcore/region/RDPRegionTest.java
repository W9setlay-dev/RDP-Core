package net.vas.rdpcore.region;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RDPRegionTest {

    @Test
    void regionCoordinatesAreCorrect() {
        RDPRegion region = new RDPRegion(5, 3);
        assertEquals(5, region.getRegionX());
        assertEquals(3, region.getRegionZ());
    }

    @Test
    void localRDPLevelClampsToNonNegative() {
        RDPRegion region = new RDPRegion(0, 0);
        region.setLocalRDPLevel(-1.0D);
        assertEquals(0.0D, region.getLocalRDPLevel());
    }

    @Test
    void pressureClampsToNonNegative() {
        RDPRegion region = new RDPRegion(0, 0);
        region.setPressure(-5.0D);
        assertEquals(0.0D, region.getPressure());
    }

    @Test
    void pressureAccumulates() {
        RDPRegion region = new RDPRegion(0, 0);
        region.addPressure(0.5D);
        region.addPressure(0.3D);
        assertEquals(0.8D, region.getPressure(), 1e-12);
    }

    @Test
    void realityAnchorCountClampsToNonNegative() {
        RDPRegion region = new RDPRegion(0, 0);
        region.setRealityAnchorCount(-10);
        assertEquals(0, region.getRealityAnchorCount());
    }
}
