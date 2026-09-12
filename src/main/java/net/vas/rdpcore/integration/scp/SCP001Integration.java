package net.vas.rdpcore.integration.scp;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.vas.rdpcore.event.RDPEvents;
import net.vas.rdpcore.integration.ModIntegration;

/**
 * Optional integration with SCP-001 Controller.
 *
 * The controller is deliberately accessed through reflection so RDP Core remains
 * loadable without the controller jar.
 */
public class SCP001Integration extends ModIntegration {
    private static final String CONTROLLER_PACKAGE = "com.w9setlay.scp001controller.";
    private static final String WORLD_DATA_CLASS = CONTROLLER_PACKAGE + "SCP001WorldData";
    private static final String SPAWN_CONTROLLER_CLASS = CONTROLLER_PACKAGE + "SCP001SpawnController";
    private static final Set<String> SCP001_ENTITY_IDS = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
            "scp_project_anomalous:scp_001_beautiful",
            "scp_project_anomalous:scp_001_spiral_path",
            "scp_project_anomalous:scp_001_factory",
            "scp_project_anomalous:scp_001_the_lock",
            "scp_project_anomalous:scp_001_the_sky_above_the_port",
            "scp_project_anomalous:scp_001_thirty_six",
            "scp_project_anomalous:scp_001_prototype",
            "scp_project_anomalous:scp001_prototype")));

    private static final String[] FOUNDATION_CLASSES = {
            "com.w9setlay.scp001controller.RDPFoundationIntegration",
            "com.w9setlay.rdpfoundation.RDPFoundationIntegration",
            "net.vas.rdpfoundation.integration.RDPFoundationIntegration",
            "net.vas.rdpfoundation.RDPFoundationIntegration"
    };

    private Method enabledMethod;
    private Method unlockedMethod;
    private Method worldDataGetMethod;
    private Method triggeredMethod;
    private Method manifestedMethod;
    private Method distortionMethod;
    private Class<?> worldDataClass;
    private int tickCounter;

    public SCP001Integration() {
        super("scp001controller");
    }

    @Override
    public boolean checkModLoaded() {
        if (!Loader.isModLoaded(modId)) {
            isLoaded = false;
            return false;
        }
        try {
            Class<?> spawnController = Class.forName(SPAWN_CONTROLLER_CLASS);
            worldDataClass = Class.forName(WORLD_DATA_CLASS);
            enabledMethod = spawnController.getMethod("isSCP001EnabledInWorld", World.class);
            unlockedMethod = spawnController.getMethod("isSCP001Unlocked", World.class);
            worldDataGetMethod = worldDataClass.getMethod("get", World.class);
            triggeredMethod = worldDataClass.getMethod("isTriggered");
            manifestedMethod = worldDataClass.getMethod("isManifested");
            isLoaded = true;
            return true;
        } catch (ClassNotFoundException e) {
            isLoaded = false;
            return false;
        } catch (NoSuchMethodException e) {
            isLoaded = false;
            logError("SCP-001 Controller API is incompatible.", e);
            return false;
        } catch (LinkageError e) {
            isLoaded = false;
            logError("SCP-001 Controller could not be linked.", e);
            return false;
        }
    }

    @Override
    public void init() {
        if (!checkModLoaded()) {
            log("SCP-001 Controller not found. Integration disabled.");
            return;
        }
        resolveFoundationMethod();
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new RDPEventListener());
        log("SCP-001 Controller integration initialized.");
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (!isLoaded || event.phase != TickEvent.Phase.END || ++tickCounter < 20) {
            return;
        }
        tickCounter = 0;
        net.minecraft.server.MinecraftServer server = net.vas.rdpcore.api.RDPAPI.getMinecraftServer();
        if (server == null) return;
        for (WorldServer world : server.worlds) {
            if (world != null) updateWorld(world);
        }
    }

    private void updateWorld(World world) {
        try {
            SCPState state = readState(world);
            if (!state.isActive()) return;
            int entities = countSCP001Entities(world);
            if (entities == 0 || distortionMethod == null) return;
            double activity = calculateActivity(state.enabled, state.unlocked, state.triggered, state.manifested, entities);
            for (Entity entity : world.loadedEntityList) {
                if (isSCP001Entity(entity)) {
                    requestDistortion(world, entity.posX, entity.posY, entity.posZ, activity, "scp001controller");
                }
            }
        } catch (ReflectiveOperationException e) {
            logError("Unable to read SCP-001 Controller state.", e);
        }
    }

    private void refreshActiveWorlds() {
        net.minecraft.server.MinecraftServer server = net.vas.rdpcore.api.RDPAPI.getMinecraftServer();
        if (server == null) return;
        for (WorldServer world : server.worlds) {
            if (world != null) updateWorld(world);
        }
    }

    private SCPState readState(World world) throws ReflectiveOperationException {
        boolean enabled = Boolean.TRUE.equals(enabledMethod.invoke(null, world));
        boolean unlocked = Boolean.TRUE.equals(unlockedMethod.invoke(null, world));
        Object data = worldDataGetMethod.invoke(null, world);
        boolean triggered = Boolean.TRUE.equals(triggeredMethod.invoke(data));
        boolean manifested = Boolean.TRUE.equals(manifestedMethod.invoke(data));
        return new SCPState(enabled, unlocked, triggered, manifested);
    }

    private void resolveFoundationMethod() {
        for (String className : FOUNDATION_CLASSES) {
            try {
                distortionMethod = Class.forName(className).getMethod(
                        "requestDistortionField", World.class, double.class, double.class, double.class,
                        double.class, double.class, String.class);
                return;
            } catch (ClassNotFoundException e) {
                // Foundation is optional.
            } catch (NoSuchMethodException e) {
                logError("RDP Foundation integration has no distortion-field API.", e);
                return;
            }
        }
    }

    private void requestDistortion(World world, double x, double y, double z, double activity, String source)
            throws ReflectiveOperationException {
        distortionMethod.invoke(null, world, x, y, z, activity, activity, source);
    }

    public static boolean isSCP001Entity(Entity entity) {
        if (entity == null) return false;
        ResourceLocation id = EntityList.getKey(entity);
        return id != null && SCP001_ENTITY_IDS.contains(id.toString());
    }

    public static int countSCP001Entities(World world) {
        if (world == null) return 0;
        int count = 0;
        for (Entity entity : world.loadedEntityList) {
            if (isSCP001Entity(entity)) count++;
        }
        return count;
    }

    public static double calculateActivity(boolean enabled, boolean unlocked, boolean triggered,
            boolean manifested, int entityCount) {
        if (!enabled || !unlocked || entityCount <= 0) return 0.0D;
        double stateMultiplier = manifested ? 1.0D : triggered ? 0.5D : 0.25D;
        return Math.min(1.0D, entityCount / 10.0D * stateMultiplier);
    }

    /**
     * React only to RDP Core events that already exist; the controller has no
     * event hooks that RDP Core needs to invent or depend on.
     */
    private final class RDPEventListener {
        @SubscribeEvent
        public void onStageChange(RDPEvents.RDPStageChangeEvent event) {
            refreshActiveWorlds();
        }

        @SubscribeEvent
        public void onAnomalySpawn(RDPEvents.AnomalySpawnEvent event) {
            refreshActiveWorlds();
        }
    }

    private static final class SCPState {
        private final boolean enabled, unlocked, triggered, manifested;
        private SCPState(boolean enabled, boolean unlocked, boolean triggered, boolean manifested) {
            this.enabled = enabled;
            this.unlocked = unlocked;
            this.triggered = triggered;
            this.manifested = manifested;
        }
        private boolean isActive() {
            return enabled && unlocked && (triggered || manifested);
        }
    }
}
