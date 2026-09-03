package net.vas.rdpcore.util;

import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Simple registry of IRdpPressureSource implementations.
 */
public class PressureRegistry {

    private static final Logger LOGGER = LogManager.getLogger("rdpcore");
    private static final List<IRdpPressureSource> sources = Collections.synchronizedList(new ArrayList<>());

    public static void register(IRdpPressureSource src) {
        if (src == null) return;
        sources.add(src);
        LOGGER.debug("Registered pressure source: {}", src.getId());
    }

    public static List<IRdpPressureSource> getSources() {
        return new ArrayList<>(sources);
    }

    public static double collectPressure(World world, Object context) {
        double total = 0.0D;
        for (IRdpPressureSource s : sources) {
            try {
                total += s.getPressure(world, context);
            } catch (Throwable t) {
                LOGGER.warn("Pressure source {} failed: {}", s.getId(), t.getMessage());
            }
        }
        return total;
    }
}
