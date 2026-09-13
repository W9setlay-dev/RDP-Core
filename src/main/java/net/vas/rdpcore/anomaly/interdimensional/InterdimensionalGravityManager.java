package net.vas.rdpcore.anomaly.interdimensional;

import java.util.Map;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.world.WorldServer;
import net.vas.rdpcore.config.RDPConfig;
import net.vas.rdpcore.core.GlobalRDPLevel;
import net.vas.rdpcore.world.RDPWorldState;

/** Bounded server tick integration for the existing interdimensional anomalies. */
public final class InterdimensionalGravityManager {
    private InterdimensionalGravityManager() { }

    public static void tick(WorldServer world, RDPWorldState state, long tick) {
        if (world == null || state == null || !RDPConfig.ENABLE_INTERDIMENSIONAL_GRAVITY) return;
        GravityFieldConfig c = RDPConfig.GRAVITY;
        if (!c.enabled || tick % c.updateIntervalTicks != 0) return;
        int anomalies = 0, entities = 0;
        GlobalRDPLevel level = state.getGlobalRDPLevel();
        for (InterdimensionalAnomaly anomaly : state.getInterdimensionalAnomalies().values()) {
            if (anomalies++ >= c.maxAnomaliesProcessedPerTick) break;
            GravityGateResult gate = GravityGateEvaluator.evaluate(anomaly, c, level.getLevel(),
                    level.getCurrentStage(), tick);
            if (!gate.isActive()) continue;
            double radius = Math.max(c.minimumRadius,
                Math.min(c.maximumRadius, anomaly.getRadius() * c.radiusMultiplier));
            for (Entity entity : world.loadedEntityList) {
                if (entities++ >= c.maxEntitiesProcessedPerTick) return;
                if (entity == null || entity.dimension != world.provider.getDimension()
                        || !GravityField.canAffect(entity, c)) continue;
                double dx = entity.posX - anomaly.getCenterX(), dz = entity.posZ - anomaly.getCenterZ();
                if (dx * dx + dz * dz > radius * radius) continue;
                String id = EntityList.getEntityString(entity);
                if (id != null && c.excludedEntities.contains(id)) continue;
                net.minecraft.util.math.Vec3d acceleration = GravityField.calculate(entity, anomaly, c,
                    gate.getInfluence()).scale(GravityField.multiplier(entity, c));
                GravityField.apply(entity, acceleration, c);
            }
        }
    }
}
