package net.vas.rdpcore.config;

import java.io.File;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * R.D.P. Core configuration system.
 * Loads configuration from a Forge config file with sensible defaults.
 */
import net.vas.rdpcore.core.GlobalRDPLevel;
import java.util.EnumMap;
import java.util.EnumSet;
import net.vas.rdpcore.entity.RealityAnchorCapability;
import net.vas.rdpcore.entity.RealityAnchorDefinition;
import net.vas.rdpcore.entity.RealityAnchorRegistry;
import net.vas.rdpcore.entity.RealityAnchorProfile;
import net.vas.rdpcore.entity.RealityAnchorProfiles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RDPConfig {
    
    private static final Logger LOGGER = LogManager.getLogger("rdpcore");
    private static Configuration config;
    
    // Global progression settings
    public static double GLOBAL_RDP_INCREMENT_PER_TICK = 0.00001D;
    public static boolean ENABLE_RDP_PROGRESSION = true;
    public static int RDP_SAVE_INTERVAL_TICKS = 20 * 60; // Every minute
    public static int SIMULATION_INTERVAL_TICKS = 200; // default simulation interval
    
    // Regional settings
    public static int REGION_SIZE_CHUNKS = 16;
    public static double REGION_PRESSURE_DECAY = 0.001D;
    public static int MAX_ANOMALIES_PER_REGION = 10;
    public static int ACTIVE_REGION_CACHE_SIZE = 1024;
    
    // Chunk rewriter integration
    public static boolean ENABLE_CHUNK_REWRITING = true;
    public static double CHUNK_REWRITE_THRESHOLD = 0.25D; // RDP-II
    public static int CHUNK_REWRITE_BUDGET_PER_TICK = 5;
    
    // SCP-001 integration
    public static boolean ENABLE_SCP001_INTEGRATION = true;
    public static double SCP001_RDP_CONTRIBUTION_MULTIPLIER = 1.0D;
    
    // Temporal anomalies
    public static boolean ENABLE_TEMPORAL_ANOMALIES = true;
    public static double TEMPORAL_ANOMALY_THRESHOLD = 0.55D; // RDP-IV
    
    // Spatial anomalies
    public static boolean ENABLE_SPATIAL_ANOMALIES = true;
    public static double SPATIAL_ANOMALY_THRESHOLD = 0.40D; // RDP-III
    
    // Dimensional anomalies
    public static boolean ENABLE_DIMENSIONAL_ANOMALIES = true;
    public static double DIMENSIONAL_ANOMALY_THRESHOLD = 0.70D; // RDP-V
    
    // Reality anchors
    public static boolean ENABLE_REALITY_ANCHORS = true;
    public static double REALITY_ANCHOR_RDP_RESISTANCE = 0.05D;
    public static double ANCHOR_MAX_EFFECT_RADIUS = 64.0D; // blocks
    public static String[] REALITY_ANCHOR_DEFINITIONS = new String[0];
    public static String[] REALITY_ANCHOR_PROFILES = new String[0];
    private static final List<String> REALITY_ANCHOR_DIAGNOSTICS = new ArrayList<>();

    public static List<String> getRealityAnchorDiagnostics() {
        return Collections.unmodifiableList(REALITY_ANCHOR_DIAGNOSTICS);
    }
    
    // Hotspot defaults
    public static int HOTSPOT_MAX_PER_WORLD = 256;
    
    // Judgement Day (RDP-X) settings
    public static boolean ENABLE_JUDGEMENT_DAY = true;
    public static double JUDGEMENT_DAY_THRESHOLD = 0.97D;
    
    // Performance & safety caps
    public static int MUTATION_NORMAL_BUDGET = 500;
    public static int MUTATION_CRITICAL_BUDGET = 2000;
    public static int MAX_ANOMALIES_ACTIVE_PER_WORLD = 1024;

    // Stage modifiers per RDP stage
    public static class StageModifiers {
        public double pressureMultiplier = 1.0D;
        public double mutationMultiplier = 1.0D;
        public double anomalyMultiplier = 1.0D;
        public double eventMultiplier = 1.0D;
        public double anchorEfficiency = 1.0D;
    }

    private static final EnumMap<GlobalRDPLevel.RDPStage, StageModifiers> stageModifiers = new EnumMap<>(GlobalRDPLevel.RDPStage.class);

    static {
        // sensible defaults: early stages dampen, late stages amplify
        for (GlobalRDPLevel.RDPStage s : GlobalRDPLevel.RDPStage.values()) {
            StageModifiers m = new StageModifiers();
            switch (s) {
                case RDP0:
                    m.pressureMultiplier = 0.5; m.mutationMultiplier = 0.5; m.anomalyMultiplier = 0.5; m.eventMultiplier = 0.5; m.anchorEfficiency = 1.0; break;
                case RDPI:
                    m.pressureMultiplier = 0.8; m.mutationMultiplier = 0.8; m.anomalyMultiplier = 0.8; m.eventMultiplier = 0.8; m.anchorEfficiency = 0.95; break;
                case RDPII:
                    m.pressureMultiplier = 1.0; m.mutationMultiplier = 1.0; m.anomalyMultiplier = 1.0; m.eventMultiplier = 1.0; m.anchorEfficiency = 0.9; break;
                case RDPIII:
                    m.pressureMultiplier = 1.2; m.mutationMultiplier = 1.2; m.anomalyMultiplier = 1.2; m.eventMultiplier = 1.1; m.anchorEfficiency = 0.8; break;
                case RDPIV:
                    m.pressureMultiplier = 1.4; m.mutationMultiplier = 1.4; m.anomalyMultiplier = 1.4; m.eventMultiplier = 1.25; m.anchorEfficiency = 0.7; break;
                case RDPV:
                    m.pressureMultiplier = 1.6; m.mutationMultiplier = 1.6; m.anomalyMultiplier = 1.6; m.eventMultiplier = 1.5; m.anchorEfficiency = 0.6; break;
                case RDPVI:
                    m.pressureMultiplier = 1.8; m.mutationMultiplier = 1.8; m.anomalyMultiplier = 1.9; m.eventMultiplier = 1.75; m.anchorEfficiency = 0.5; break;
                case RDPVII:
                    m.pressureMultiplier = 2.0; m.mutationMultiplier = 2.0; m.anomalyMultiplier = 2.5; m.eventMultiplier = 2.0; m.anchorEfficiency = 0.35; break;
                case RDPX:
                    m.pressureMultiplier = 3.0; m.mutationMultiplier = 3.0; m.anomalyMultiplier = 4.0; m.eventMultiplier = 4.0; m.anchorEfficiency = 0.1; break;
            }
            stageModifiers.put(s, m);
        }
    }

    public static StageModifiers getStageModifiers(GlobalRDPLevel.RDPStage stage) {
        return stageModifiers.getOrDefault(stage, new StageModifiers());
    }

    public static void load() {
        try {
            File configFile = new File("config/rdpcore.cfg");
            config = new Configuration(configFile);
            config.load();
            
            // Global progression
            GLOBAL_RDP_INCREMENT_PER_TICK = config.getFloat(
                "Global RDP increment per tick", 
                Configuration.CATEGORY_GENERAL,
                0.00001F,
                0.0F,
                0.01F,
                "How much global RDP increases each tick (20 ticks = 1 second)"
            );
            
            ENABLE_RDP_PROGRESSION = config.getBoolean(
                "Enable RDP progression",
                Configuration.CATEGORY_GENERAL,
                true,
                "Whether RDP globally progresses over time"
            );
            
            // Regional settings
            REGION_SIZE_CHUNKS = config.getInt(
                "Region size in chunks",
                Configuration.CATEGORY_GENERAL,
                16,
                4,
                64,
                "Chunk-based regions (16x16 chunks = 256x256 blocks)"
            );
            
            REGION_PRESSURE_DECAY = config.getFloat(
                "Region pressure decay",
                Configuration.CATEGORY_GENERAL,
                0.001F,
                0.0F,
                0.1F,
                "How quickly regional pressure decreases per tick"
            );
            
            // Chunk rewriter
            ENABLE_CHUNK_REWRITING = config.getBoolean(
                "Enable chunk rewriting",
                Configuration.CATEGORY_GENERAL,
                true,
                "Whether chunk mutations occur"
            );
            
            CHUNK_REWRITE_THRESHOLD = config.getFloat(
                "Chunk rewrite RDP threshold",
                Configuration.CATEGORY_GENERAL,
                0.25F,
                0.0F,
                1.0F,
                "RDP level at which chunk rewriting begins (0.25 = RDP-II)"
            );
            
            // Integration flags
            ENABLE_SCP001_INTEGRATION = config.getBoolean(
                "Enable SCP-001 integration",
                Configuration.CATEGORY_GENERAL,
                true,
                "Whether SCP-001 controller is integrated"
            );
            
            ENABLE_TEMPORAL_ANOMALIES = config.getBoolean(
                "Enable temporal anomalies",
                Configuration.CATEGORY_GENERAL,
                true,
                ""
            );
            
            ENABLE_SPATIAL_ANOMALIES = config.getBoolean(
                "Enable spatial anomalies",
                Configuration.CATEGORY_GENERAL,
                true,
                ""
            );
            
            ENABLE_DIMENSIONAL_ANOMALIES = config.getBoolean(
                "Enable dimensional anomalies",
                Configuration.CATEGORY_GENERAL,
                true,
                ""
            );
            
            ENABLE_REALITY_ANCHORS = config.getBoolean(
                "Enable reality anchors",
                Configuration.CATEGORY_GENERAL,
                true,
                "Whether reality anchors can resist RDP progression"
            );

            REALITY_ANCHOR_DEFINITIONS = config.getStringList(
                "Anchor definitions",
                "realityAnchors",
                new String[0],
                "blockId[:metadata],radius,strength,capabilities separated by |"
            );
            REALITY_ANCHOR_PROFILES = config.getStringList("Anchor profiles", "realityAnchors",
                new String[0], "name,radiusMultiplier,strengthMultiplier,capabilities separated by |");
            ANCHOR_MAX_EFFECT_RADIUS = config.getFloat("Maximum anchor effect radius", "realityAnchors",
                64.0F, 0.0F, 4096.0F, "Upper bound used by the default anchor profile");
            RealityAnchorRegistry.clear();
            RealityAnchorProfiles.clear();
            REALITY_ANCHOR_DIAGNOSTICS.clear();
            for (String profile : REALITY_ANCHOR_PROFILES) registerAnchorProfile(profile);
            for (String definition : REALITY_ANCHOR_DEFINITIONS) {
                registerAnchorDefinition(definition);
            }
            
            ENABLE_JUDGEMENT_DAY = config.getBoolean(
                "Enable Judgement Day",
                Configuration.CATEGORY_GENERAL,
                true,
                "Whether RDP-X end-game is active"
            );
            
            if (config.hasChanged()) {
                config.save();
            }

            LOGGER.info("R.D.P. Core configuration loaded successfully.");
            
        } catch (Exception e) {
            LOGGER.error("Failed to load R.D.P. Core configuration!", e);
        }
    }

    private static void registerAnchorDefinition(String value) {
        try {
            String[] fields = value.split(",", -1);
            String blockValue = fields[0].trim();
            int metadata = -1;
            int separator = blockValue.lastIndexOf(':');
            if (separator > 0 && separator + 1 < blockValue.length()) {
                String suffix = blockValue.substring(separator + 1);
                if (suffix.matches("\\d+")) {
                    metadata = Integer.parseInt(suffix);
                    blockValue = blockValue.substring(0, separator);
                }
            }
            double radius = fields.length > 1 ? Double.parseDouble(fields[1]) : ANCHOR_MAX_EFFECT_RADIUS;
            double strength = fields.length > 2 ? Double.parseDouble(fields[2]) : 1.0D;
            String diagnostic = RealityAnchorDefinition.validate(blockValue, metadata, radius, strength);
            if (diagnostic != null) {
                REALITY_ANCHOR_DIAGNOSTICS.add(value + ": " + diagnostic);
                LOGGER.warn("Ignoring invalid reality anchor definition '{}': {}", value, diagnostic);
                return;
            }
            if (!RealityAnchorRegistry.isKnownBlockId(blockValue)) {
                REALITY_ANCHOR_DIAGNOSTICS.add(value + ": block is not currently registered (it may be supplied by a late-loading mod)");
            }
            EnumSet<RealityAnchorCapability> capabilities = EnumSet.noneOf(RealityAnchorCapability.class);
            if (fields.length > 3) {
                for (String name : fields[3].split("\\|")) {
                    capabilities.add(RealityAnchorCapability.valueOf(name.trim().toUpperCase()));
                }
            }
            String profile = fields.length > 4 ? fields[4].trim() : "default";
            RealityAnchorRegistry.register(new RealityAnchorDefinition(
                blockValue, metadata, radius, strength, capabilities, profile));
        } catch (RuntimeException ex) {
            REALITY_ANCHOR_DIAGNOSTICS.add(value + ": " + ex.getMessage());
            LOGGER.warn("Ignoring invalid reality anchor definition '{}': {}", value, ex.getMessage());
        }
    }

    private static void registerAnchorProfile(String value) {
        try {
            String[] fields = value.split(",", -1);
            if (fields.length < 3) throw new IllegalArgumentException("profile requires name,radiusMultiplier,strengthMultiplier");
            EnumSet<RealityAnchorCapability> capabilities = EnumSet.noneOf(RealityAnchorCapability.class);
            if (fields.length > 3) for (String name : fields[3].split("\\|"))
                capabilities.add(RealityAnchorCapability.valueOf(name.trim().toUpperCase()));
            RealityAnchorProfiles.register(new RealityAnchorProfile(fields[0].trim(),
                Double.parseDouble(fields[1]), Double.parseDouble(fields[2]), capabilities));
        } catch (RuntimeException ex) {
            REALITY_ANCHOR_DIAGNOSTICS.add(value + ": " + ex.getMessage());
            LOGGER.warn("Ignoring invalid reality anchor profile '{}': {}", value, ex.getMessage());
        }
    }
    
    public static void save() {
        if (config != null && config.hasChanged()) {
            config.save();
        }
    }
}
