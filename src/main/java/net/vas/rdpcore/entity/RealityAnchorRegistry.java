package net.vas.rdpcore.entity;

import java.util.Collections;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public final class RealityAnchorRegistry {
    private static final Map<String, RealityAnchorDefinition> DEFINITIONS = new ConcurrentHashMap<>();
    private static final List<String> DIAGNOSTICS = new ArrayList<>();

    private RealityAnchorRegistry() {}

    public static void register(RealityAnchorDefinition definition) {
        if (definition == null) {
            throw new IllegalArgumentException("definition must not be null");
        }
        String key = key(definition.getBlockId(), definition.getMetadata());
        if (DEFINITIONS.containsKey(key)) {
            String diagnostic = "duplicate anchor definition: " + key;
            synchronized (DIAGNOSTICS) { DIAGNOSTICS.add(diagnostic); }
            throw new IllegalArgumentException(diagnostic);
        }
        DEFINITIONS.put(key, definition);
    }

    public static RealityAnchorDefinition find(String blockId, int metadata) {
        RealityAnchorDefinition exact = DEFINITIONS.get(key(blockId, metadata));
        return exact != null ? exact : DEFINITIONS.get(key(blockId, -1));
    }

    public static Map<String, RealityAnchorDefinition> getDefinitions() {
        return Collections.unmodifiableMap(DEFINITIONS);
    }

    public static void clear() {
        DEFINITIONS.clear();
        synchronized (DIAGNOSTICS) { DIAGNOSTICS.clear(); }
    }

    public static List<String> getDiagnostics() {
        synchronized (DIAGNOSTICS) { return Collections.unmodifiableList(new ArrayList<>(DIAGNOSTICS)); }
    }

    public static boolean isKnownBlockId(String blockId) {
        try {
            return ForgeRegistries.BLOCKS.containsKey(new ResourceLocation(blockId));
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private static String key(String blockId, int metadata) {
        return blockId + "#" + metadata;
    }
}
