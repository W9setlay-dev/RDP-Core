package net.vas.rdpcore.util;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Simple telemetry counters for the RDP simulation.
 */
public class Telemetry {

    public static final AtomicLong simulationCycles = new AtomicLong(0);
    public static final AtomicLong lastSimulationDurationMs = new AtomicLong(0);
    public static final AtomicLong regionalUpdates = new AtomicLong(0);
    public static final AtomicLong pressureCalcDurationMs = new AtomicLong(0);
    public static final AtomicLong anomalyDurationMs = new AtomicLong(0);
    public static final AtomicLong eventDurationMs = new AtomicLong(0);
    public static final AtomicLong mutationPlanningDurationMs = new AtomicLong(0);
    public static final AtomicLong mutationsProcessedLast = new AtomicLong(0);

    public static void recordSimulation(long durationMs, long regionsProcessed, long pressureMs, long anomalyMs, long eventMs, long mutationMs, long mutationsProcessed) {
        simulationCycles.incrementAndGet();
        lastSimulationDurationMs.set(durationMs);
        regionalUpdates.set(regionsProcessed);
        pressureCalcDurationMs.set(pressureMs);
        anomalyDurationMs.set(anomalyMs);
        eventDurationMs.set(eventMs);
        mutationPlanningDurationMs.set(mutationMs);
        mutationsProcessedLast.set(mutationsProcessed);
    }
}
