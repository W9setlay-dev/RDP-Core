package net.vas.rdpcore.anomaly.interdimensional;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * Persistent, world-independent state for one dimensional anomaly.
 */
public final class InterdimensionalAnomaly implements INBTSerializable<NBTTagCompound> {
    private final UUID id;
    private final int sourceDimension;
    private final int hostDimension;
    private final double centerX;
    private final double centerY;
    private final double centerZ;
    private final long seed;
    private final String profileId;
    private final int maxAffectedChunks;
    private double radius;
    private double maxRadius;
    private double pressure;
    private AnomalyStage stage;
    private AnomalyLifecycle lifecycle;
    private boolean opened;
    private boolean closed;
    private long createdAt;
    private long updatedAt;
    private final Set<Long> affectedChunks = new LinkedHashSet<Long>();

    public InterdimensionalAnomaly(UUID id, int sourceDimension, int hostDimension, double centerX,
                                   double centerY, double centerZ, long seed, String profileId,
                                   double radius, double maxRadius, int maxAffectedChunks,
                                   long createdAt) {
        if (id == null || profileId == null || profileId.trim().isEmpty()) {
            throw new IllegalArgumentException("id and profileId are required");
        }
        if (!Double.isFinite(radius) || radius < 0.0D || !Double.isFinite(maxRadius)
                || maxRadius < radius || maxRadius > AnomalyConfig.MAX_RADIUS_CAP
                || maxAffectedChunks <= 0 || maxAffectedChunks > AnomalyConfig.MAX_AFFECTED_CHUNKS_CAP) {
            throw new IllegalArgumentException("invalid anomaly bounds");
        }
        this.id = id;
        this.sourceDimension = sourceDimension;
        this.hostDimension = hostDimension;
        this.centerX = centerX;
        this.centerY = centerY;
        this.centerZ = centerZ;
        this.seed = seed;
        this.profileId = profileId;
        this.radius = radius;
        this.maxRadius = maxRadius;
        this.maxAffectedChunks = maxAffectedChunks;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
        this.stage = AnomalyStage.NASCENT;
        this.lifecycle = AnomalyLifecycle.DORMANT;
    }

    public static InterdimensionalAnomaly create(UUID id, AnomalyProfile profile, double x, double y,
                                                  double z, long seed, long timestamp,
                                                  AnomalyConfig config) {
        if (profile == null) throw new IllegalArgumentException("profile is required");
        double maxRadius = Math.min(profile.getMaximumRadius(), config.getMaxRadius());
        return new InterdimensionalAnomaly(id, profile.getSourceDimension(), profile.getHostDimension(),
                x, y, z, seed, profile.getId(), Math.min(profile.getInitialRadius(), maxRadius),
                maxRadius, config.getMaxAffectedChunks(), timestamp);
    }

    public static InterdimensionalAnomaly fromNBT(NBTTagCompound tag) {
        UUID id = UUID.fromString(tag.getString("id"));
        InterdimensionalAnomaly anomaly = new InterdimensionalAnomaly(id,
                tag.getInteger("sourceDimension"), tag.getInteger("hostDimension"),
                tag.getDouble("centerX"), tag.getDouble("centerY"), tag.getDouble("centerZ"),
                tag.getLong("seed"), tag.getString("profile"), tag.getDouble("radius"),
                tag.getDouble("maxRadius"), Math.max(1, tag.getInteger("maxAffectedChunks")),
                tag.getLong("createdAt"));
        anomaly.deserializeNBT(tag);
        return anomaly;
    }

    public void applyPressure(double delta, long timestamp, AnomalyConfig config) {
        if (!Double.isFinite(delta)) throw new IllegalArgumentException("pressure delta must be finite");
        if (closed) return;
        pressure = Math.max(0.0D, Math.min(AnomalyConfig.MAX_PRESSURE_CAP, pressure + delta));
        updatedAt = timestamp;
        if (pressure >= config.getCollapsePressure()) {
            stage = AnomalyStage.COLLAPSED;
            lifecycle = AnomalyLifecycle.COLLAPSING;
        } else if (pressure >= config.getCriticalPressure()) {
            stage = AnomalyStage.CRITICAL;
            lifecycle = AnomalyLifecycle.ACTIVE;
        } else if (pressure >= config.getActivationPressure()) {
            stage = AnomalyStage.UNSTABLE;
            lifecycle = AnomalyLifecycle.ACTIVE;
            opened = true;
        }
        radius = Math.min(maxRadius, Math.max(radius, maxRadius * pressure));
    }

    public void close(long timestamp) {
        closed = true;
        lifecycle = AnomalyLifecycle.CLOSED;
        updatedAt = timestamp;
    }

    public void trackAffectedChunk(long chunkKey) {
        if (affectedChunks.size() < maxAffectedChunks) affectedChunks.add(chunkKey);
    }

    public void trackAffectedChunks(Set<Long> chunks) {
        if (chunks == null) return;
        for (Long chunk : chunks) {
            if (chunk == null || affectedChunks.size() >= maxAffectedChunks) break;
            affectedChunks.add(chunk);
        }
    }

    public UUID getId() { return id; }
    public int getSourceDimension() { return sourceDimension; }
    public int getHostDimension() { return hostDimension; }
    public double getCenterX() { return centerX; }
    public double getCenterY() { return centerY; }
    public double getCenterZ() { return centerZ; }
    public long getSeed() { return seed; }
    public String getProfileId() { return profileId; }
    public double getRadius() { return radius; }
    public double getMaxRadius() { return maxRadius; }
    public double getPressure() { return pressure; }
    public AnomalyStage getStage() { return stage; }
    public AnomalyLifecycle getLifecycle() { return lifecycle; }
    public boolean isOpened() { return opened; }
    public boolean isClosed() { return closed; }
    public long getCreatedAt() { return createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public Set<Long> getAffectedChunks() { return Collections.unmodifiableSet(affectedChunks); }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("id", id.toString());
        tag.setInteger("sourceDimension", sourceDimension);
        tag.setInteger("hostDimension", hostDimension);
        tag.setDouble("centerX", centerX);
        tag.setDouble("centerY", centerY);
        tag.setDouble("centerZ", centerZ);
        tag.setLong("seed", seed);
        tag.setString("profile", profileId);
        tag.setDouble("radius", radius);
        tag.setDouble("maxRadius", maxRadius);
        tag.setDouble("pressure", pressure);
        tag.setString("stage", stage.name());
        tag.setString("lifecycle", lifecycle.name());
        tag.setBoolean("opened", opened);
        tag.setBoolean("closed", closed);
        tag.setLong("createdAt", createdAt);
        tag.setLong("updatedAt", updatedAt);
        tag.setInteger("maxAffectedChunks", maxAffectedChunks);
        NBTTagList chunks = new NBTTagList();
        for (Long key : affectedChunks) {
            NBTTagCompound chunk = new NBTTagCompound();
            chunk.setLong("key", key.longValue());
            chunks.appendTag(chunk);
        }
        tag.setTag("affectedChunks", chunks);
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag) {
        affectedChunks.clear();
        pressure = Math.max(0.0D, Math.min(1.0D, tag.getDouble("pressure")));
        radius = Math.max(0.0D, Math.min(maxRadius, tag.getDouble("radius")));
        stage = parseStage(tag.getString("stage"));
        lifecycle = parseLifecycle(tag.getString("lifecycle"));
        opened = tag.getBoolean("opened");
        closed = tag.getBoolean("closed");
        createdAt = tag.getLong("createdAt");
        updatedAt = tag.getLong("updatedAt");
        NBTTagList chunks = tag.getTagList("affectedChunks", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < chunks.tagCount() && affectedChunks.size() < maxAffectedChunks; i++) {
            affectedChunks.add(chunks.getCompoundTagAt(i).getLong("key"));
        }
    }

    private static AnomalyStage parseStage(String value) {
        if (value == null || value.isEmpty()) return AnomalyStage.NASCENT;
        return AnomalyStage.valueOf(value);
    }

    private static AnomalyLifecycle parseLifecycle(String value) {
        if (value == null || value.isEmpty()) return AnomalyLifecycle.DORMANT;
        return AnomalyLifecycle.valueOf(value);
    }
}
