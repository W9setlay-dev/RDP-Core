package net.vas.rdpcore.world;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * WorldSavedData wrapper for RDPWorldState persistence.
 */
public class RDPWorldSavedData extends WorldSavedData {

    private static final Logger LOGGER = LogManager.getLogger("rdpcore");
    public static final String DATA_NAME = "rdpcore_world_state";

    private RDPWorldState state;
    private final World world;

    public RDPWorldSavedData(World world) {
        super(DATA_NAME);
        this.world = world;
        this.state = new RDPWorldState(world);
    }

    public RDPWorldState getState() { return state; }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        if (nbt == null) return;
        try {
            state.deserializeNBT(nbt.getCompoundTag("rdp"));
            LOGGER.info("RDP world state loaded for {}", world.getWorldInfo().getWorldName());
        } catch (Throwable t) {
            LOGGER.warn("Failed to read RDP world data: {}", t.getMessage());
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        try {
            NBTTagCompound tag = state.serializeNBT();
            compound.setTag("rdp", tag);
        } catch (Throwable t) {
            LOGGER.warn("Failed to write RDP world data: {}", t.getMessage());
        }
        return compound;
    }

    public void markDirtyAndSave() {
        this.markDirty();
    }
}
