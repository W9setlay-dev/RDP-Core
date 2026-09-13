package net.vas.rdpcore.anomaly.interdimensional;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.math.Vec3d;

/** Pure local field calculation and bounded server-side application. */
public final class GravityField {
    private GravityField() { }

    public static Vec3d calculate(Entity entity, InterdimensionalAnomaly anomaly,
                                  GravityFieldConfig config, double gateInfluence) {
        if (entity == null || anomaly == null || config == null || gateInfluence <= 0) return Vec3d.ZERO;
        double dx = anomaly.getCenterX() - entity.posX, dy = anomaly.getCenterY() - entity.posY;
        double dz = anomaly.getCenterZ() - entity.posZ;
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        double radius = Math.max(config.minimumRadius,
            Math.min(config.maximumRadius, anomaly.getRadius() * config.radiusMultiplier));
        if (!Double.isFinite(distance) || radius <= 0 || distance > radius) return Vec3d.ZERO;
        double falloff = config.falloff.apply(distance, radius);
        double strength = config.strength * gateInfluence
                * (config.edgeStrength + (1.0D - config.edgeStrength) * falloff);
        if (config.coreEnabled && distance <= Math.max(0, config.coreRadius))
            strength *= Math.min(4.0D, Math.max(0.0D, config.coreMultiplier));
        if (!Double.isFinite(strength) || strength <= 0) return Vec3d.ZERO;
        Vec3d radial = distance < 1e-7D ? new Vec3d(0, -1, 0)
                : new Vec3d(dx / distance, dy / distance, dz / distance);
        Vec3d direction;
        switch (config.mode) {
            case RADIAL: direction = radial; break;
            case ANTI_RADIAL: direction = radial.scale(-1); break;
            case VECTOR:
            case AXIAL:
                direction = safeDirection(config.directionX, config.directionY, config.directionZ);
                break;
            case RADIAL_PLUS_ROTATION:
                direction = blend(new Vec3d(0, -1, 0), radial, config.rotationStrength);
                break;
            case RADIAL_PLUS_DOWNWARD:
                direction = blend(new Vec3d(0, -1, 0), radial, config.rotationEnabled ? config.rotationStrength : 1.0D);
                break;
            case NORMAL_ADDITIVE:
            default: direction = new Vec3d(0, -1, 0); break;
        }
        Vec3d result = direction.scale(Math.min(config.maximumAcceleration, strength));
        if (config.orbitEnabled && config.orbitStrength > 0 && distance > 1e-7D) {
            Vec3d tangent = new Vec3d(-radial.z, 0, radial.x);
            result = result.add(tangent.scale(Math.min(config.orbitMaxSpeed, config.orbitStrength * strength)));
        }
        return finite(result) ? result : Vec3d.ZERO;
    }

    public static boolean canAffect(Entity entity, GravityFieldConfig c) {
        if (entity == null || c == null || entity.isDead) return false;
        if (entity instanceof EntityPlayer) {
            EntityPlayer p = (EntityPlayer) entity;
            if (p.capabilities.isCreativeMode && !c.affectCreativePlayers) return false;
            if (p.isSpectator() && !c.affectSpectators) return false;
            if (!c.affectPlayers) return false;
        } else if (entity instanceof EntityItem && !c.affectItems) return false;
        else if (entity instanceof EntityFallingBlock && !c.affectFallingBlocks) return false;
        else if ((entity instanceof EntityArrow || entity instanceof EntityThrowable) && !c.affectProjectiles) return false;
        else if (entity instanceof EntityMinecart && !c.affectVehicles) return false;
        else if (entity instanceof EntityLivingBase && !c.affectLivingEntities) return false;
        String id = EntityList.getEntityString(entity);
        if (id != null && c.excludedEntities.contains(id)) return false;
        return true;
    }

    public static double multiplier(Entity entity, GravityFieldConfig c) {
        String id = entity == null ? null : EntityList.getEntityString(entity);
        Double value = id == null ? null : c.entityMultipliers.get(id);
        return value == null ? 1.0D : Math.max(0.0D, Math.min(4.0D, value));
    }

    public static void apply(Entity entity, Vec3d acceleration, GravityFieldConfig c) {
        if (entity == null || acceleration == null || c == null) return;
        double cap = Math.max(0.0D, Math.min(0.25D, c.maximumVelocityChangePerTick));
        double x = clamp(acceleration.x, -cap, cap), y = clamp(acceleration.y, -cap, cap);
        double z = clamp(acceleration.z, -cap, cap);
        entity.motionX = clamp(entity.motionX + x, -c.maximumHorizontalVelocity, c.maximumHorizontalVelocity);
        entity.motionY = clamp(entity.motionY + y, -c.maximumVerticalVelocity, c.maximumVerticalVelocity);
        entity.motionZ = clamp(entity.motionZ + z, -c.maximumHorizontalVelocity, c.maximumHorizontalVelocity);
        entity.velocityChanged = true;
    }

    private static Vec3d blend(Vec3d a, Vec3d b, double amount) {
        double t = Math.max(0, Math.min(1, amount));
        return safeNormalize(a.scale(1 - t).add(b.scale(t)), a);
    }
    private static Vec3d safeDirection(double x, double y, double z) {
        double n = Math.sqrt(x*x + y*y + z*z);
        return n < 1e-9 || !Double.isFinite(n) ? new Vec3d(0, -1, 0) : new Vec3d(x/n, y/n, z/n);
    }
    private static Vec3d safeNormalize(Vec3d v, Vec3d fallback) {
        double n = Math.sqrt(v.x * v.x + v.y * v.y + v.z * v.z);
        return n < 1e-9 || !Double.isFinite(n) ? fallback : v.scale(1.0D / n);
    }
    private static boolean finite(Vec3d v) {
        return Double.isFinite(v.x) && Double.isFinite(v.y) && Double.isFinite(v.z);
    }
    private static double clamp(double v, double lo, double hi) { return Math.max(lo, Math.min(hi, v)); }
}
