package dev.kvnmtz.headdatabase.fabric;

import dev.kvnmtz.headdatabase.config.HeadDatabaseServerConfig;
import dev.kvnmtz.headdatabase.fabric.config.HeadDatabaseFabricServerConfig;
import net.fabricmc.api.ModInitializer;

import dev.kvnmtz.headdatabase.HeadDatabase;

@SuppressWarnings("unused")
public final class HeadDatabaseFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        HeadDatabaseFabricServerConfig.load();
        HeadDatabaseServerConfig.setInstance(HeadDatabaseFabricServerConfig.getInstance());

        HeadDatabase.init();
    }
}
