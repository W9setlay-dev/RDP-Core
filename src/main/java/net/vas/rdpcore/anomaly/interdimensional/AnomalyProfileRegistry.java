package net.vas.rdpcore.anomaly.interdimensional;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class AnomalyProfileRegistry {
    public static final String NETHER_LEAK = "nether_leak";
    private final Map<String, AnomalyProfile> profiles = new LinkedHashMap<String, AnomalyProfile>();

    public AnomalyProfileRegistry() {
        register(new AnomalyProfile(NETHER_LEAK, -1, 0, 8.0D, 256.0D, 0.015D, 0.002D));
    }

    public void register(AnomalyProfile profile) {
        if (profile == null) throw new IllegalArgumentException("profile is required");
        profiles.put(profile.getId(), profile);
    }

    public AnomalyProfile get(String id) { return profiles.get(id); }
    public boolean contains(String id) { return profiles.containsKey(id); }
    public Map<String, AnomalyProfile> asMap() {
        return Collections.unmodifiableMap(profiles);
    }
}
