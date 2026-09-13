package net.vas.rdpcore.client;

import net.minecraftforge.common.MinecraftForge;
import net.vas.rdpcore.cosmology.CosmologyManager;

public class ClientProxy extends CommonProxy {
    @Override public void preInit() {
        CosmologyManager.init();
        MinecraftForge.EVENT_BUS.register(new CosmologyRenderer());
    }
}
