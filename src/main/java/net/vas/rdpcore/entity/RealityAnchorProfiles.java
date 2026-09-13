package net.vas.rdpcore.entity;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class RealityAnchorProfiles {
    private static final Map<String, RealityAnchorProfile> PROFILES = new ConcurrentHashMap<>();
    static {
        register(new RealityAnchorProfile("default", 1.0D, 1.0D, null));
    }
    private RealityAnchorProfiles() {}
    public static void register(RealityAnchorProfile profile) {
        if (profile == null) throw new IllegalArgumentException("profile must not be null");
        PROFILES.put(profile.getName(), profile);
    }
    public static RealityAnchorProfile find(String name) {
        return PROFILES.get(name == null || name.trim().isEmpty() ? "default" : name.trim());
    }
    public static Map<String, RealityAnchorProfile> getProfiles() {
        return Collections.unmodifiableMap(PROFILES);
    }
    public static void clear() {
        RealityAnchorProfile defaultProfile = PROFILES.get("default");
        PROFILES.clear();
        if (defaultProfile != null) PROFILES.put("default", defaultProfile);
    }
}
